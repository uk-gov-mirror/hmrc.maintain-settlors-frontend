/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import config.FrontendAppConfig
import connectors.TrustStoreConnector
import controllers.actions.StandardActionSets
import forms.AddASettlorFormProvider
import javax.inject.Inject
import models.DeedOfVariation.AdditionToWillTrust
import models.requests.DataRequest
import models.{AddASettlor, TypeOfTrust}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.AddASettlorViewHelper
import views.html.{AddASettlorView, MaxedOutSettlorsView}

import scala.concurrent.{ExecutionContext, Future}

class AddASettlorController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       standardActionSets: StandardActionSets,
                                       val controllerComponents: MessagesControllerComponents,
                                       val appConfig: FrontendAppConfig,
                                       trustStoreConnector: TrustStoreConnector,
                                       trustService: TrustService,
                                       addAnotherFormProvider: AddASettlorFormProvider,
                                       repository: PlaybackRepository,
                                       addAnotherView: AddASettlorView,
                                       completeView: MaxedOutSettlorsView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val addAnotherForm : Form[AddASettlor] = addAnotherFormProvider()

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      for {
        settlors <- trustService.getSettlors(request.userAnswers.utr)
        updatedAnswers <- Future.fromTry(request.userAnswers.cleanup)
        _ <- repository.set(updatedAnswers)
      } yield {
        
        val settlorRows = new AddASettlorViewHelper(settlors).rows

        if (settlors.nonMaxedOutOptions.isEmpty) {
          Ok(completeView(
            trustDescription,
            inProgressSettlors = settlorRows.inProgress,
            completeSettlors = settlorRows.complete,
            heading = settlors.addToHeading
          ))
        } else {
          Ok(addAnotherView(
            form = addAnotherForm,
            trustDescription,
            inProgressSettlors = settlorRows.inProgress,
            completeSettlors = settlorRows.complete,
            heading = settlors.addToHeading,
            maxedOut = settlors.maxedOutOptions.map(x => x.messageKey)
          ))
        }
      }
  }

  def submit(): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      trustService.getSettlors(request.userAnswers.utr).flatMap { settlors =>
        addAnotherForm.bindFromRequest().fold(
          (formWithErrors: Form[_]) => {

            val rows = new AddASettlorViewHelper(settlors).rows

            Future.successful(BadRequest(
              addAnotherView(
                formWithErrors,
                trustDescription,
                rows.inProgress,
                rows.complete,
                settlors.addToHeading,
                maxedOut = settlors.maxedOutOptions.map(x => x.messageKey)
              )
            ))
          },
          {
            case AddASettlor.YesNow =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.cleanup)
                _ <- repository.set(updatedAnswers)
              } yield Redirect(controllers.routes.FeatureNotAvailableController.onPageLoad())

            case AddASettlor.YesLater =>
              Future.successful(Redirect(appConfig.maintainATrustOverview))

            case AddASettlor.NoComplete =>
              for {
                _ <- trustStoreConnector.setTaskComplete(request.userAnswers.utr)
              } yield {
                Redirect(appConfig.maintainATrustOverview)
              }
          }
        )
      }
  }

  def submitComplete(): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      for {
        _ <- trustStoreConnector.setTaskComplete(request.userAnswers.utr)
      } yield {
        Redirect(appConfig.maintainATrustOverview)
      }
  }

  private def trustDescription(implicit request: DataRequest[AnyContent]): String = {

    val description = (request.userAnswers.trustType, request.userAnswers.deedOfVariation) match {
      case (TypeOfTrust.WillTrustOrIntestacyTrust, _) => "willTrust"
      case (TypeOfTrust.IntervivosSettlementTrust, _) => "intervivosTrust"
      case (TypeOfTrust.DeedOfVariation, Some(AdditionToWillTrust)) => "deedOfVariationInAdditionToWill"
      case (TypeOfTrust.DeedOfVariation, _) => "deedOfVariation"
      case (TypeOfTrust.EmployeeRelated, _) => "employeeRelated"
      case (TypeOfTrust.FlatManagementTrust, _) => "flatManagementTrust"
      case (TypeOfTrust.HeritageTrust, _) => "heritageTrust"
    }

    request.messages(messagesApi)(s"trustDescription.$description")
  }
}
