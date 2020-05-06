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

package controllers.individual.deceased

import config.{ErrorHandler, FrontendAppConfig}
import connectors.{TrustConnector, TrustStoreConnector}
import controllers.actions._
import controllers.actions.individual.deceased.NameRequiredAction
import extractors.DeceasedSettlorExtractor
import javax.inject.Inject
import models.BpMatchStatus.FullyMatched
import models.UserAnswers
import models.settlors.Settlors
import pages.individual.deceased.{AdditionalSettlorsYesNoPage, BpMatchStatusPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.mappers.DeceasedSettlorMapper
import utils.print.DeceasedSettlorPrintHelper
import viewmodels.AnswerSection
import views.html.individual.deceased.CheckDetailsView

import scala.concurrent.{ExecutionContext, Future}

class CheckDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        standardActionSets: StandardActionSets,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CheckDetailsView,
                                        service: TrustService,
                                        connector: TrustConnector,
                                        trustStoreConnector: TrustStoreConnector,
                                        val appConfig: FrontendAppConfig,
                                        playbackRepository: PlaybackRepository,
                                        printHelper: DeceasedSettlorPrintHelper,
                                        mapper: DeceasedSettlorMapper,
                                        nameAction: NameRequiredAction,
                                        extractor: DeceasedSettlorExtractor,
                                        errorHandler: ErrorHandler
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def render(userAnswers: UserAnswers,
                     name: String,
                     hasAdditionalSettlors: Boolean)(implicit request: Request[AnyContent]): Result = {
    val section: AnswerSection = printHelper(userAnswers, name, hasAdditionalSettlors)
    Ok(view(
      section,
      name,
      userAnswers.get(BpMatchStatusPage) match {
        case Some(FullyMatched) => true
        case _ => false
      },
      userAnswers.isDateOfDeathRecorded
    ))
  }

  def extractAndRender(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      service.getSettlors(request.userAnswers.utr) flatMap {
        case Settlors(individuals, businesses, Some(deceased)) =>
          for {
            hasAdditionalSettlors <- Future.successful(individuals.nonEmpty || businesses.nonEmpty)
            extractedF <- Future.fromTry(extractor(request.userAnswers, deceased, hasAdditionalSettlors))
            _ <- playbackRepository.set(extractedF)
          } yield {
            render(extractedF, deceased.name.displayName, hasAdditionalSettlors)
          }
        case Settlors(_, _, None) =>
          throw new Exception("Deceased Settlor Information not found")

      }
  }

  def renderFromUserAnswers(): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction).async {
    implicit request =>
      service.getSettlors(request.userAnswers.utr).flatMap { settlors =>
        Future.successful(render(
          request.userAnswers,
          request.settlorName,
          settlors.settlor.nonEmpty && settlors.settlorCompany.nonEmpty
        ))
      }
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      mapper(request.userAnswers).map {
        deceasedSettlor =>
          connector.amendDeceasedSettlor(request.userAnswers.utr, deceasedSettlor).flatMap(_ =>
            service.getSettlors(request.userAnswers.utr).flatMap { settlors =>
              (settlors.settlor.isEmpty && settlors.settlorCompany.isEmpty, request.userAnswers.get(AdditionalSettlorsYesNoPage)) match {
                case (false, _) | (_, Some(true)) => Future.successful(Redirect(controllers.routes.AddASettlorController.onPageLoad()))
                case _ => trustStoreConnector.setTaskComplete(request.userAnswers.utr).map(_ =>
                  Redirect(appConfig.maintainATrustOverview)
                )
              }
            }
          )
      }.getOrElse(Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate)))
  }
}
