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
import models.{CheckMode, IdCard, Name, NonUkAddress, NormalMode, Passport, UkAddress}
import pages.individual.living._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class AddIndividualSettlorPrintHelperSpec extends SpecBase {

  val name: Name = Name("First", Some("Middle"), "Last")
  val ukAddress = UkAddress("value 1", "value 2", None, None, "AB1 1AB")
  val nonUkAddress = NonUkAddress("value 1", "value 2", None, "DE")

  "IndividualSettlorPrintHelper" must {

    "generate individual settlor section for all possible data" in {

      val helper = injector.instanceOf[IndividualSettlorPrintHelper]

      val userAnswers = emptyUserAnswers
        .set(NamePage, name).success.value
        .set(DateOfBirthYesNoPage, true).success.value
        .set(DateOfBirthPage, LocalDate.of(2010, 10, 10)).success.value
        .set(NationalInsuranceNumberYesNoPage, true).success.value
        .set(NationalInsuranceNumberPage, "AA000000A").success.value
        .set(AddressYesNoPage, true).success.value
        .set(LiveInTheUkYesNoPage, true).success.value
        .set(UkAddressPage, ukAddress).success.value
        .set(NonUkAddressPage, nonUkAddress).success.value
        .set(PassportDetailsYesNoPage, true).success.value
        .set(PassportDetailsPage, Passport("GB", "1", LocalDate.of(2030, 10, 10))).success.value
        .set(IdCardDetailsYesNoPage, true).success.value
        .set(IdCardDetailsPage, IdCard("GB", "1", LocalDate.of(2030, 10, 10))).success.value
        .set(StartDatePage, LocalDate.of(2020, 1, 1)).success.value

      val result = helper(userAnswers, provisional = true, name.displayName)
      result mustBe AnswerSection(
        headingKey = None,
        rows = Seq(
          AnswerRow(label = Html(messages("livingSettlor.name.checkYourAnswersLabel")), answer = Html("First Middle Last"), changeUrl = Some(controllers.individual.living.routes.NameController.onPageLoad(NormalMode).url)),
          AnswerRow(label = Html(messages("livingSettlor.dateOfBirthYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = Some(controllers.individual.living.routes.DateOfBirthYesNoController.onPageLoad(NormalMode).url)),
          AnswerRow(label = Html(messages("livingSettlor.dateOfBirth.checkYourAnswersLabel", name.displayName)), answer = Html("10 October 2010"), changeUrl = Some(controllers.individual.living.routes.DateOfBirthController.onPageLoad(NormalMode).url)),
          AnswerRow(label = Html(messages("livingSettlor.nationalInsuranceNumberYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = Some(controllers.individual.living.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode).url)),
          AnswerRow(label = Html(messages("livingSettlor.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName)), answer = Html("AA 00 00 00 A"), changeUrl = Some(controllers.individual.living.routes.NationalInsuranceNumberController.onPageLoad(NormalMode).url)),
          AnswerRow(label = Html(messages("livingSettlor.addressYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = Some(controllers.individual.living.routes.AddressYesNoController.onPageLoad(NormalMode).url)),
          AnswerRow(label = Html(messages("livingSettlor.liveInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = Some(controllers.individual.living.routes.LiveInTheUkYesNoController.onPageLoad(NormalMode).url)),
          AnswerRow(label = Html(messages("livingSettlor.ukAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = Some(controllers.individual.living.routes.UkAddressController.onPageLoad(NormalMode).url)),
          AnswerRow(label = Html(messages("livingSettlor.nonUkAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = Some(controllers.individual.living.routes.NonUkAddressController.onPageLoad(NormalMode).url)),
          AnswerRow(label = Html(messages("livingSettlor.passportDetailsYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = Some(controllers.individual.living.routes.PassportDetailsYesNoController.onPageLoad(NormalMode).url)),
          AnswerRow(label = Html(messages("livingSettlor.passportDetails.checkYourAnswersLabel", name.displayName)), answer = Html("United Kingdom<br />1<br />10 October 2030"), changeUrl = Some(controllers.individual.living.routes.PassportDetailsController.onPageLoad(NormalMode).url)),
          AnswerRow(label = Html(messages("livingSettlor.idCardDetailsYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = Some(controllers.individual.living.routes.IdCardDetailsYesNoController.onPageLoad(NormalMode).url)),
          AnswerRow(label = Html(messages("livingSettlor.idCardDetails.checkYourAnswersLabel", name.displayName)), answer = Html("United Kingdom<br />1<br />10 October 2030"), changeUrl = Some(controllers.individual.living.routes.IdCardDetailsController.onPageLoad(NormalMode).url)),
          AnswerRow(label = Html(messages("livingSettlor.startDate.checkYourAnswersLabel", name.displayName)), answer = Html("1 January 2020"), changeUrl = Some(controllers.individual.living.routes.StartDateController.onPageLoad().url))
        )
      )
    }
  }
}
