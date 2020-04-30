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

package navigation

import controllers.business.{routes => rts}
import javax.inject.Inject
import models.TypeOfTrust.EmployeeRelated
import models.{CheckMode, Mode, NormalMode, TypeOfTrust, UserAnswers}
import pages.business._
import pages.{Page, QuestionPage}
import play.api.mvc.Call

class BusinessSettlorNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, trustType: TypeOfTrust): Call =
    routes(mode, trustType)(page)(userAnswers)

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    nextPage(page, mode, userAnswers, EmployeeRelated)

  override def nextPage(page: Page, userAnswers: UserAnswers): Call =
    nextPage(page, NormalMode, userAnswers)

  private def simpleNavigation(mode: Mode): PartialFunction[Page, Call] = {
    case NamePage => rts.UtrYesNoController.onPageLoad(mode)
    case CompanyTypePage => rts.CompanyTimeController.onPageLoad(mode)
    case StartDatePage => controllers.business.add.routes.CheckDetailsController.onPageLoad()
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case UtrYesNoPage => ua =>
      yesNoNav(ua, UtrYesNoPage, rts.UtrController.onPageLoad(mode), rts.AddressYesNoController.onPageLoad(mode))
    case LiveInTheUkYesNoPage => ua =>
      yesNoNav(ua, LiveInTheUkYesNoPage, rts.UkAddressController.onPageLoad(mode), rts.NonUkAddressController.onPageLoad(mode))
  }

  private def navigationWithCheckAndTrustType(mode: Mode, trustType: TypeOfTrust): PartialFunction[Page, UserAnswers => Call] = {
    mode match {
      case NormalMode => {
        case UtrPage | UkAddressPage | NonUkAddressPage => ua =>
          trustTypeNav(mode, ua, trustType)
        case CompanyTimePage => _ =>
          rts.StartDateController.onPageLoad()
        case AddressYesNoPage => ua =>
          yesNoNav(ua, AddressYesNoPage, rts.LiveInTheUkYesNoController.onPageLoad(mode), rts.StartDateController.onPageLoad())
      }
      case CheckMode => {
        case UtrPage | UkAddressPage | NonUkAddressPage => ua =>
          trustTypeNav(mode, ua, trustType)
        case CompanyTimePage => ua =>
          checkDetailsRoute(ua)
        case AddressYesNoPage => ua =>
          yesNoNav(ua, AddressYesNoPage, rts.LiveInTheUkYesNoController.onPageLoad(mode), checkDetailsRoute(ua))
      }
    }
  }

  private def trustTypeNav(mode: Mode, ua: UserAnswers, trustType: TypeOfTrust): Call = {
    trustType match {
      case EmployeeRelated => rts.CompanyTypeController.onPageLoad(mode)
      case _ =>
        mode match {
          case NormalMode =>
            rts.StartDateController.onPageLoad()
          case CheckMode =>
            checkDetailsRoute(ua)
        }
    }
  }

  private def yesNoNav(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call = {
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.SessionExpiredController.onPageLoad())
  }

  private def checkDetailsRoute(answers: UserAnswers): Call = {
    answers.get(IndexPage) match {
      case None => controllers.routes.SessionExpiredController.onPageLoad()
      case Some(x) =>
        controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(x)
    }
  }

  private def routes(mode: Mode, trustType: TypeOfTrust): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode) andThen (c => (_: UserAnswers) => c) orElse
      yesNoNavigation(mode) orElse
      navigationWithCheckAndTrustType(mode, trustType)

}

