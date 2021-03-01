/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.actions.StandardActionSets
import javax.inject.Inject
import models.{TrustDetails, UserAnswers}
import pages.AdditionalSettlorsYesNoPage
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.FeatureFlagService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 actions: StandardActionSets,
                                 cacheRepository : PlaybackRepository,
                                 connector: TrustConnector,
                                 featureFlagService: FeatureFlagService
                               )
                               (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val logger: Logger = Logger(getClass)

  def onPageLoad(utr: String): Action[AnyContent] = (actions.auth andThen actions.saveSession(utr) andThen actions.getData).async {
      implicit request =>
        logger.info(s"[Session ID: ${Session.id(hc)}][UTR: $utr] user has started to maintain settlors")

        def newUserAnswers(details: TrustDetails,
                           utr: String,
                           isDateOfDeathRecorded: Boolean,
                           is5mldEnabled: Boolean
                          ) = UserAnswers(
            internalId = request.user.internalId,
            identifier = utr,
            whenTrustSetup = details.startDate,
            trustType = details.typeOfTrust,
            deedOfVariation = details.deedOfVariation,
            isDateOfDeathRecorded = isDateOfDeathRecorded,
            is5mldEnabled = is5mldEnabled,
            isTaxable = details.trustTaxable.getOrElse(true)
        )

        for {
          details <- connector.getTrustDetails(utr)
          is5mldEnabled <- featureFlagService.is5mldEnabled()
          allSettlors <- connector.getSettlors(utr)
          isDateOfDeathRecorded <- connector.getIsDeceasedSettlorDateOfDeathRecorded(utr)
          ua <- Future.successful {
            request.userAnswers.getOrElse {
              newUserAnswers(details, utr, isDateOfDeathRecorded.value, is5mldEnabled)
            }
          }
          _ <- cacheRepository.set(ua)
        } yield {

          val showAddToPage = allSettlors.hasLivingSettlors || ua.get(AdditionalSettlorsYesNoPage).contains(true)

          if (showAddToPage) {
            Redirect(controllers.routes.AddASettlorController.onPageLoad())
          } else {
            Redirect(controllers.individual.deceased.routes.CheckDetailsController.extractAndRender())
          }

        }
    }
}
