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

package controllers.business.amend

import config.{ErrorHandler, FrontendAppConfig}
import connectors.TrustConnector
import controllers.actions._
import controllers.actions.business.NameRequiredAction
import extractors.BusinessSettlorExtractor
import javax.inject.Inject
import models.UserAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.mappers.BusinessSettlorMapper
import utils.print.BusinessSettlorPrintHelper
import viewmodels.AnswerSection
import views.html.business.amend.CheckDetailsView

import scala.concurrent.{ExecutionContext, Future}

class CheckDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        standardActionSets: StandardActionSets,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CheckDetailsView,
                                        service: TrustService,
                                        connector: TrustConnector,
                                        val appConfig: FrontendAppConfig,
                                        playbackRepository: PlaybackRepository,
                                        printHelper: BusinessSettlorPrintHelper,
                                        mapper: BusinessSettlorMapper,
                                        nameAction: NameRequiredAction,
                                        extractor: BusinessSettlorExtractor,
                                        errorHandler: ErrorHandler
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def render(userAnswers: UserAnswers,
                     index: Int,
                     name: String)
                    (implicit request: Request[AnyContent]): Result = {
    val section: AnswerSection = printHelper(userAnswers, provisional = false, name)
    Ok(view(section, index))
  }

  def extractAndRender(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      service.getBusinessSettlor(request.userAnswers.utr, index) flatMap {
        settlor =>
          for {
            extractedF <- Future.fromTry(extractor(request.userAnswers, settlor, index))
            _ <- playbackRepository.set(extractedF)
          } yield {
              render(extractedF, index, settlor.name)
          }
      }
  }

  def renderFromUserAnswers(index: Int) : Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>
      render(request.userAnswers, index, request.settlorName)
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      mapper(request.userAnswers).map {
        business =>
          connector.amendBusinessSettlor(request.userAnswers.utr, index, business).map(_ =>
            Redirect(controllers.routes.AddASettlorController.onPageLoad())
          )
      }.getOrElse(Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate)))
  }
}
