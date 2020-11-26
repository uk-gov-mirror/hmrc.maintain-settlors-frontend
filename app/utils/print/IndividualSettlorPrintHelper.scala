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

import com.google.inject.Inject
import controllers.individual.living.add.{routes => addRts}
import controllers.individual.living.amend.{routes => amendRts}
import controllers.individual.living.{routes => rts}
import models.{CheckMode, NormalMode, UserAnswers}
import pages.individual.living._
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection}

class IndividualSettlorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter,
                                             countryOptions: CountryOptions
                                            ) {

  def apply(userAnswers: UserAnswers, provisional: Boolean, settlorName: String)(implicit messages: Messages): AnswerSection = {

    val bound = answerRowConverter.bind(userAnswers, settlorName, countryOptions)

    lazy val add: Seq[AnswerRow] = Seq(
      bound.nameQuestion(NamePage, "livingSettlor.name", Some(rts.NameController.onPageLoad(NormalMode).url)),
      bound.yesNoQuestion(DateOfBirthYesNoPage, "livingSettlor.dateOfBirthYesNo", Some(rts.DateOfBirthYesNoController.onPageLoad(NormalMode).url)),
      bound.dateQuestion(DateOfBirthPage, "livingSettlor.dateOfBirth", Some(rts.DateOfBirthController.onPageLoad(NormalMode).url)),
      bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "livingSettlor.nationalInsuranceNumberYesNo", Some(rts.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode).url)),
      bound.ninoQuestion(NationalInsuranceNumberPage, "livingSettlor.nationalInsuranceNumber", Some(rts.NationalInsuranceNumberController.onPageLoad(NormalMode).url)),
      bound.yesNoQuestion(AddressYesNoPage, "livingSettlor.addressYesNo", Some(rts.AddressYesNoController.onPageLoad(NormalMode).url)),
      bound.yesNoQuestion(LiveInTheUkYesNoPage, "livingSettlor.liveInTheUkYesNo", Some(rts.LiveInTheUkYesNoController.onPageLoad(NormalMode).url)),
      bound.addressQuestion(UkAddressPage, "livingSettlor.ukAddress", Some(rts.UkAddressController.onPageLoad(NormalMode).url)),
      bound.addressQuestion(NonUkAddressPage, "livingSettlor.nonUkAddress", Some(rts.NonUkAddressController.onPageLoad(NormalMode).url)),
      bound.yesNoQuestion(PassportDetailsYesNoPage, "livingSettlor.passportDetailsYesNo", Some(addRts.PassportDetailsYesNoController.onPageLoad(NormalMode).url)),
      bound.passportDetailsQuestion(PassportDetailsPage, "livingSettlor.passportDetails", Some(addRts.PassportDetailsController.onPageLoad(NormalMode).url)),
      bound.yesNoQuestion(IdCardDetailsYesNoPage, "livingSettlor.idCardDetailsYesNo", Some(addRts.IdCardDetailsYesNoController.onPageLoad(NormalMode).url)),
      bound.idCardDetailsQuestion(IdCardDetailsPage, "livingSettlor.idCardDetails", Some(addRts.IdCardDetailsController.onPageLoad(NormalMode).url)),
      bound.dateQuestion(StartDatePage, "livingSettlor.startDate", Some(addRts.StartDateController.onPageLoad().url))
    ).flatten

    lazy val amend: Seq[AnswerRow] = Seq(
      bound.nameQuestion(NamePage, "livingSettlor.name", Some(rts.NameController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(DateOfBirthYesNoPage, "livingSettlor.dateOfBirthYesNo", Some(rts.DateOfBirthYesNoController.onPageLoad(CheckMode).url)),
      bound.dateQuestion(DateOfBirthPage, "livingSettlor.dateOfBirth", Some(rts.DateOfBirthController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "livingSettlor.nationalInsuranceNumberYesNo", Some(rts.NationalInsuranceNumberYesNoController.onPageLoad(CheckMode).url)),
      bound.ninoQuestion(NationalInsuranceNumberPage, "livingSettlor.nationalInsuranceNumber", Some(rts.NationalInsuranceNumberController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(AddressYesNoPage, "livingSettlor.addressYesNo", Some(rts.AddressYesNoController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(LiveInTheUkYesNoPage, "livingSettlor.liveInTheUkYesNo", Some(rts.LiveInTheUkYesNoController.onPageLoad(CheckMode).url)),
      bound.addressQuestion(UkAddressPage, "livingSettlor.ukAddress", Some(rts.UkAddressController.onPageLoad(CheckMode).url)),
      bound.addressQuestion(NonUkAddressPage, "livingSettlor.nonUkAddress", Some(rts.NonUkAddressController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(PassportOrIdCardDetailsYesNoPage, "livingSettlor.passportOrIdCardDetailsYesNo", Some(amendRts.PassportOrIdCardDetailsYesNoController.onPageLoad(CheckMode).url)),
      bound.passportOrIdCardDetailsQuestion(PassportOrIdCardDetailsPage, "livingSettlor.passportOrIdCardDetails", Some(amendRts.PassportOrIdCardDetailsController.onPageLoad(CheckMode).url))
    ).flatten

    AnswerSection(
      None,
      if (provisional) add else amend
    )
  }
}
