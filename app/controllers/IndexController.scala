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

import connectors.TrustConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import javax.inject.Inject
import models.UserAnswers
import pages.AdditionalSettlorsYesNoPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 identifierAction: IdentifierAction,
                                 getData: DataRetrievalAction,
                                 repo : PlaybackRepository,
                                 connector: TrustConnector)
                               (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(utr: String): Action[AnyContent] =

    (identifierAction andThen getData).async {
      implicit request =>
        for {
          details <- connector.getTrustDetails(utr)
          allSettlors <- connector.getSettlors(utr)
          isDateOfDeathRecorded <- connector.getIsDeceasedSettlorDateOfDeathRecorded(utr)
          ua <- Future.successful(request.userAnswers.getOrElse(
            UserAnswers(
              internalId = request.user.internalId,
              utr = utr,
              whenTrustSetup = details.startDate,
              trustType = details.typeOfTrust,
              deedOfVariation = details.deedOfVariation,
              isDateOfDeathRecorded = isDateOfDeathRecorded.value
            )
          ))
          _ <- repo.set(ua)
        } yield {
          (allSettlors.hasAdditionalSettlors, ua.get(AdditionalSettlorsYesNoPage)) match {
            case (true, _) | (_, Some(true)) =>
              Redirect(controllers.routes.AddASettlorController.onPageLoad())
            case _ =>
              Redirect(controllers.individual.deceased.routes.CheckDetailsController.extractAndRender())
          }
        }
    }
}
