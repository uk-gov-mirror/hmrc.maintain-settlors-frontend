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

import controllers.individual.deceased.{routes => rts}
import javax.inject.Inject
import models.{Mode, TypeOfTrust, UserAnswers}
import pages.individual.deceased._
import pages.{Page, QuestionPage}
import play.api.mvc.Call

class DeceasedSettlorNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, userAnswers: UserAnswers): Call =
    routes(page)(userAnswers)

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(page)(userAnswers)

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, trustType: TypeOfTrust): Call = {
    routes(page)(userAnswers)
  }

  private val simpleNavigation: PartialFunction[Page, Call] = {
    case NamePage => rts.DateOfDeathYesNoController.onPageLoad()
    case DateOfDeathPage => rts.DateOfBirthYesNoController.onPageLoad()
    case DateOfBirthPage => rts.NationalInsuranceNumberYesNoController.onPageLoad()

  }

  private val yesNoNavigation: PartialFunction[Page, UserAnswers => Call] = {
    case DateOfDeathYesNoPage => ua =>
      yesNoNav(ua, DateOfDeathYesNoPage, rts.DateOfDeathController.onPageLoad(), rts.DateOfBirthYesNoController.onPageLoad())
    case DateOfBirthYesNoPage => ua =>
      yesNoNav(ua, DateOfBirthYesNoPage, rts.DateOfBirthController.onPageLoad(), rts.NationalInsuranceNumberYesNoController.onPageLoad())
    case NationalInsuranceNumberYesNoPage => ua =>
      yesNoNav(ua, NationalInsuranceNumberYesNoPage, rts.NationalInsuranceNumberController.onPageLoad(), rts.AddressYesNoController.onPageLoad())
    case NationalInsuranceNumberPage => ua =>
      checkDetailsRoute(ua)
    case AddressYesNoPage => ua =>
      yesNoNav(ua, AddressYesNoPage, rts.LivedInTheUkYesNoController.onPageLoad(), checkDetailsRoute(ua))
    case LivedInTheUkYesNoPage => ua =>
      yesNoNav(ua, LivedInTheUkYesNoPage, rts.UkAddressController.onPageLoad(), rts.NonUkAddressController.onPageLoad())
    case UkAddressPage => ua =>
      checkDetailsRoute(ua)
    case NonUkAddressPage => ua =>
      checkDetailsRoute(ua)
  }

  def yesNoNav(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call = {
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.SessionExpiredController.onPageLoad())
  }

  def checkDetailsRoute(answers: UserAnswers): Call = {
    answers.get(IndexPage) match {
      case None => controllers.routes.SessionExpiredController.onPageLoad()
      case Some(x) =>
        rts.CheckDetailsController.renderFromUserAnswers(x)
    }
  }

  val routes: PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation andThen (c => (_:UserAnswers) => c) orElse
      yesNoNavigation

}

