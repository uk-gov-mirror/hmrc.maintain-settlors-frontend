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

package utils.print

import java.time.LocalDate

import base.SpecBase
import models.{Name, NonUkAddress, UkAddress}
import pages.individual.deceased._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class DeceasedSettlor01PrintHelperSpec extends SpecBase {

  val name: Name = Name("First", Some("Middle"), "Last")
  val ukAddress = UkAddress("value 1", "value 2", None, None, "AB1 1AB")
  val nonUkAddress = NonUkAddress("value 1", "value 2", None, "DE")

  "DeceasedSettlor01PrintHelper" must {

    "generate deceased settlor section for all possible data when date of death is known to ETMP" in {

      val helper = injector.instanceOf[DeceasedSettlor01PrintHelper]

      val userAnswers = emptyUserAnswers
        .set(NamePage, name).success.value
        .set(DateOfDeathYesNoPage, true).success.value
        .set(DateOfDeathPage, LocalDate.of(2011, 10, 10)).success.value
        .set(DateOfBirthYesNoPage, true).success.value
        .set(DateOfBirthPage, LocalDate.of(2010, 10, 10)).success.value
        .set(NationalInsuranceNumberYesNoPage, true).success.value
        .set(NationalInsuranceNumberPage, "AA000000A").success.value
        .set(AddressYesNoPage, true).success.value
        .set(LivedInTheUkYesNoPage, true).success.value
        .set(UkAddressPage, ukAddress).success.value
        .set(NonUkAddressPage, nonUkAddress).success.value

      val result = helper(userAnswers, name.displayName, isDateOfDeathRecorded = true)
      result mustBe AnswerSection(
        headingKey = None,
        rows = Seq(
          AnswerRow(label = Html(messages("deceasedSettlor.name.checkYourAnswersLabel")), answer = Html("First Middle Last"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.dateOfDeathYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.dateOfDeath.checkYourAnswersLabel", name.displayName)), answer = Html("10 October 2011"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.dateOfBirthYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.dateOfBirth.checkYourAnswersLabel", name.displayName)), answer = Html("10 October 2010"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.nationalInsuranceNumberYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName)), answer = Html("AA 00 00 00 A"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.addressYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.liveInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.ukAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.nonUkAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = None)
        )
      )
    }

    "generate deceased settlor section for all possible data when date of death is not known to ETMP" in {

      val helper = injector.instanceOf[DeceasedSettlor01PrintHelper]

      val userAnswers = emptyUserAnswers
        .set(NamePage, name).success.value
        .set(DateOfDeathYesNoPage, true).success.value
        .set(DateOfDeathPage, LocalDate.of(2011, 10, 10)).success.value
        .set(DateOfBirthYesNoPage, true).success.value
        .set(DateOfBirthPage, LocalDate.of(2010, 10, 10)).success.value
        .set(NationalInsuranceNumberYesNoPage, true).success.value
        .set(NationalInsuranceNumberPage, "AA000000A").success.value
        .set(AddressYesNoPage, true).success.value
        .set(LivedInTheUkYesNoPage, true).success.value
        .set(UkAddressPage, ukAddress).success.value
        .set(NonUkAddressPage, nonUkAddress).success.value

      val result = helper(userAnswers, name.displayName, isDateOfDeathRecorded = false)
      result mustBe AnswerSection(
        headingKey = None,
        rows = Seq(
          AnswerRow(label = Html(messages("deceasedSettlor.name.checkYourAnswersLabel")), answer = Html("First Middle Last"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.dateOfDeathYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = Some(controllers.individual.deceased.routes.DateOfDeathYesNoController.onPageLoad().url)),
          AnswerRow(label = Html(messages("deceasedSettlor.dateOfDeath.checkYourAnswersLabel", name.displayName)), answer = Html("10 October 2011"), changeUrl = Some(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad().url)),
          AnswerRow(label = Html(messages("deceasedSettlor.dateOfBirthYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.dateOfBirth.checkYourAnswersLabel", name.displayName)), answer = Html("10 October 2010"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.nationalInsuranceNumberYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName)), answer = Html("AA 00 00 00 A"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.addressYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.liveInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.ukAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = None),
          AnswerRow(label = Html(messages("deceasedSettlor.nonUkAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = None)
        )
      )
    }
  }
}
