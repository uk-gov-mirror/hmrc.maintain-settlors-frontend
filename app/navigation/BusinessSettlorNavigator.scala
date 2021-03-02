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

package navigation

import controllers.business.{routes => rts}
import javax.inject.Inject
import models.TypeOfTrust.EmployeeRelated
import models.{CheckMode, Mode, NormalMode, TypeOfTrust, UserAnswers}
import pages.Page
import pages.business._
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

  private def yesNoNavigation(mode: Mode, trustType: TypeOfTrust): PartialFunction[Page, UserAnswers => Call] = {
    case UtrYesNoPage => ua =>
      yesNoNav(ua, UtrYesNoPage, rts.UtrController.onPageLoad(mode), navigateAwayFromUtrPages(mode, trustType, ua))
    case CountryOfResidenceYesNoPage => ua =>
      yesNoNav(ua, CountryOfResidenceYesNoPage, rts.CountryOfResidenceInTheUkYesNoController.onPageLoad(mode), navigateToEndPages(mode, trustType, ua))
    case CountryOfResidenceInTheUkYesNoPage => ua =>
      yesNoNav(ua, CountryOfResidenceInTheUkYesNoPage, navigateAwayFromResidencePages(mode, trustType, ua), rts.CountryOfResidenceController.onPageLoad(mode))
    case LiveInTheUkYesNoPage => ua =>
      yesNoNav(ua, LiveInTheUkYesNoPage, rts.UkAddressController.onPageLoad(mode), rts.NonUkAddressController.onPageLoad(mode))
  }

  private def navigationWithCheckAndTrustType(mode: Mode, trustType: TypeOfTrust): PartialFunction[Page, UserAnswers => Call] = {
    mode match {
      case NormalMode => {
        case UtrPage => ua =>
          navigateAwayFromUtrPages(mode, trustType, ua)
        case UkAddressPage | NonUkAddressPage => ua =>
          navigateToEndPages(mode, trustType, ua)
        case CompanyTimePage => _ =>
          rts.StartDateController.onPageLoad()
        case AddressYesNoPage => ua =>
          yesNoNav(ua, AddressYesNoPage, rts.LiveInTheUkYesNoController.onPageLoad(mode), navigateToEndPages(mode, trustType, ua))
        case CountryOfResidencePage => ua =>
          navigateAwayFromResidencePages(mode, trustType, ua)
      }
      case CheckMode => {
        case UtrPage => ua =>
          navigateAwayFromUtrPages(mode, trustType, ua)
        case UtrPage | UkAddressPage | NonUkAddressPage => ua =>
          navigateToEndPages(mode, trustType, ua)
        case CompanyTimePage => ua =>
          checkDetailsRoute(ua)
        case AddressYesNoPage => ua =>
          yesNoNav(ua, AddressYesNoPage, rts.LiveInTheUkYesNoController.onPageLoad(mode), checkDetailsRoute(ua))
        case CountryOfResidencePage => ua =>
          navigateAwayFromResidencePages(mode, trustType, ua)
      }
    }
  }

  private def navigateAwayFromUtrPages(mode: Mode, trustType: TypeOfTrust, answers: UserAnswers): Call = {
    (answers.is5mldEnabled, answers.get(UtrYesNoPage)) match {
      case (true, _) => rts.CountryOfResidenceYesNoController.onPageLoad(mode)
      case (false, Some(true)) => navigateToEndPages(mode, trustType, answers)
      case (false, _) => rts.AddressYesNoController.onPageLoad(mode)
    }
  }

  private def navigateAwayFromResidencePages(mode: Mode, trustType: TypeOfTrust, answers: UserAnswers): Call = {
    answers.get(UtrYesNoPage) match {
      case Some(true) => navigateToEndPages(mode, trustType, answers)
      case _ => rts.AddressYesNoController.onPageLoad(mode)
    }
  }

  private def navigateToEndPages(mode:Mode, trustType: TypeOfTrust, ua: UserAnswers): Call = {
    (mode, trustType) match {
      case (NormalMode, EmployeeRelated) => rts.CompanyTypeController.onPageLoad(mode)
      case (NormalMode, _) => rts.StartDateController.onPageLoad()
      case (CheckMode, EmployeeRelated) => rts.CompanyTypeController.onPageLoad(mode)
      case (CheckMode, _) => checkDetailsRoute(ua)
    }
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
      yesNoNavigation(mode, trustType) orElse
      navigationWithCheckAndTrustType(mode, trustType)

}

