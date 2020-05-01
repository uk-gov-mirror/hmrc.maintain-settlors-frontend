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
import models.UserAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.mappers.DeceasedSettlorMapper
import utils.print.DeceasedSettlor01PrintHelper
import viewmodels.AnswerSection
import views.html.individual.deceased.CheckDetails01View

import scala.concurrent.{ExecutionContext, Future}

class CheckDetails01Controller @Inject()(
                                        override val messagesApi: MessagesApi,
                                        standardActionSets: StandardActionSets,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CheckDetails01View,
                                        service: TrustService,
                                        connector: TrustConnector,
                                        trustStoreConnector: TrustStoreConnector,
                                        val appConfig: FrontendAppConfig,
                                        playbackRepository: PlaybackRepository,
                                        printHelper: DeceasedSettlor01PrintHelper,
                                        mapper: DeceasedSettlorMapper,
                                        nameAction: NameRequiredAction,
                                        extractor: DeceasedSettlorExtractor,
                                        errorHandler: ErrorHandler
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def render(userAnswers: UserAnswers,
                     name: String,
                     isDateOfDeathRecorded: Boolean
                    )(implicit request: Request[AnyContent]): Result = {
    val section: AnswerSection = printHelper(userAnswers, name, isDateOfDeathRecorded)
    Ok(view(section))
  }

  def extractAndRender(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      service.getDeceasedSettlor(request.userAnswers.utr) flatMap {
        case Some(deceasedSettlor) =>
          for {
            extractedF <- Future.fromTry(extractor(request.userAnswers, deceasedSettlor))
            _ <- playbackRepository.set(extractedF)
          } yield {
            render(extractedF, deceasedSettlor.name.displayName, request.userAnswers.isDateOfDeathRecorded)
          }
        case None => throw new Exception("Deceased Settlor Information not found")
      }
  }

  def renderFromUserAnswers() : Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>
      render(request.userAnswers, request.settlorName, request.userAnswers.isDateOfDeathRecorded)
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      mapper(request.userAnswers).map {
        deceasedSettlor =>
          connector.amendDeceasedSettlor(request.userAnswers.utr, deceasedSettlor).flatMap(_ =>
            service.getSettlors(request.userAnswers.utr).flatMap { settlors =>
              if (settlors.settlor.isEmpty && settlors.settlorCompany.isEmpty) {
                for {
                  _ <- trustStoreConnector.setTaskComplete(request.userAnswers.utr)
                } yield Redirect(appConfig.maintainATrustOverview)
              } else {
                Future.successful(Redirect(controllers.routes.AddASettlorController.onPageLoad()))
              }
            }
          )
      }.getOrElse(Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate)))
  }
}
