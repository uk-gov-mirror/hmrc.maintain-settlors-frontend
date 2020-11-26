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

import controllers.individual.living.amend.{routes => amendRts}
import controllers.individual.living.add.{routes => addRts}
import controllers.individual.living.{routes => rts}
import javax.inject.Inject
import models.{CheckMode, Mode, NormalMode, TypeOfTrust, UserAnswers}
import pages.Page
import pages.individual.living._
import play.api.mvc.Call

class IndividualSettlorNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, trustType: TypeOfTrust): Call =
    nextPage(page, mode, userAnswers)

  override def nextPage(page: Page, userAnswers: UserAnswers): Call =
    nextPage(page, NormalMode, userAnswers)

  private def simpleNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case NamePage => _ => rts.DateOfBirthYesNoController.onPageLoad(mode)
    case DateOfBirthPage => _ => rts.NationalInsuranceNumberYesNoController.onPageLoad(mode)
    case PassportDetailsPage | IdCardDetailsPage => _ => addRts.StartDateController.onPageLoad()
    case PassportOrIdCardDetailsPage => ua => checkDetailsRoute(ua)
    case StartDatePage => _ => addRts.CheckDetailsController.onPageLoad()
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case DateOfBirthYesNoPage => ua =>
      yesNoNav(ua, DateOfBirthYesNoPage, rts.DateOfBirthController.onPageLoad(mode), rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
    case NationalInsuranceNumberYesNoPage => ua =>
      yesNoNav(ua, NationalInsuranceNumberYesNoPage, rts.NationalInsuranceNumberController.onPageLoad(mode), rts.AddressYesNoController.onPageLoad(mode))
    case LiveInTheUkYesNoPage => ua =>
      yesNoNav(ua, LiveInTheUkYesNoPage, rts.UkAddressController.onPageLoad(mode), rts.NonUkAddressController.onPageLoad(mode))
    case PassportDetailsYesNoPage => ua =>
      yesNoNav(ua, PassportDetailsYesNoPage, addRts.PassportDetailsController.onPageLoad(), addRts.IdCardDetailsYesNoController.onPageLoad())
    case IdCardDetailsYesNoPage => ua =>
      yesNoNav(ua, IdCardDetailsYesNoPage, addRts.IdCardDetailsController.onPageLoad(), addRts.StartDateController.onPageLoad())
    case PassportOrIdCardDetailsYesNoPage => ua =>
      yesNoNav(ua, PassportOrIdCardDetailsYesNoPage, amendRts.PassportOrIdCardDetailsController.onPageLoad(), checkDetailsRoute(ua))
  }

  private def navigationWithCheck(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    mode match {
      case NormalMode => {
        case NationalInsuranceNumberPage => _ =>
          addRts.StartDateController.onPageLoad()
        case AddressYesNoPage => ua =>
          yesNoNav(ua, AddressYesNoPage, rts.LiveInTheUkYesNoController.onPageLoad(mode), addRts.StartDateController.onPageLoad())
        case UkAddressPage | NonUkAddressPage => _ =>
          addRts.PassportDetailsYesNoController.onPageLoad()
      }
      case CheckMode => {
        case NationalInsuranceNumberPage => ua =>
          checkDetailsRoute(ua)
        case AddressYesNoPage => ua =>
          yesNoNav(ua, AddressYesNoPage, rts.LiveInTheUkYesNoController.onPageLoad(mode), checkDetailsRoute(ua))
        case UkAddressPage | NonUkAddressPage => _ =>
          amendRts.PassportOrIdCardDetailsYesNoController.onPageLoad()
      }
    }
  }

  private def checkDetailsRoute(answers: UserAnswers): Call = {
    answers.get(IndexPage) match {
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
      case Some(x) =>
        amendRts.CheckDetailsController.renderFromUserAnswers(x)
    }
  }

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode) orElse
      yesNoNavigation(mode) orElse
      navigationWithCheck(mode)

}

