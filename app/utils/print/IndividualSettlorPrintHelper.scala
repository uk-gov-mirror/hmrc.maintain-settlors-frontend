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
import models.{CheckMode, NormalMode, UserAnswers}
import pages.individual.living._
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection}

class IndividualSettlorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter,
                                             countryOptions: CountryOptions
                                            ) {

  def apply(userAnswers: UserAnswers, provisional: Boolean, settlorName: String)(implicit messages: Messages) = {

    val bound = answerRowConverter.bind(userAnswers, settlorName, countryOptions)

    val add: Seq[AnswerRow] = Seq(
        bound.nameQuestion(NamePage, "livingSettlor.name", Some(controllers.individual.living.routes.NameController.onPageLoad(NormalMode).url)),
        bound.yesNoQuestion(DateOfBirthYesNoPage, "livingSettlor.dateOfBirthYesNo", Some(controllers.individual.living.routes.DateOfBirthYesNoController.onPageLoad(NormalMode).url)),
        bound.dateQuestion(DateOfBirthPage, "livingSettlor.dateOfBirth", Some(controllers.individual.living.routes.DateOfBirthController.onPageLoad(NormalMode).url)),
        bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "livingSettlor.nationalInsuranceNumberYesNo", Some(controllers.individual.living.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode).url)),
        bound.ninoQuestion(NationalInsuranceNumberPage, "livingSettlor.nationalInsuranceNumber", Some(controllers.individual.living.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode).url)),
        bound.yesNoQuestion(AddressYesNoPage, "livingSettlor.addressYesNo", Some(controllers.individual.living.routes.AddressYesNoController.onPageLoad(NormalMode).url)),
        bound.yesNoQuestion(LiveInTheUkYesNoPage, "livingSettlor.liveInTheUkYesNo", Some(controllers.individual.living.routes.LiveInTheUkYesNoController.onPageLoad(NormalMode).url)),
        bound.addressQuestion(UkAddressPage, "livingSettlor.ukAddress", Some(controllers.individual.living.routes.UkAddressController.onPageLoad(NormalMode).url)),
        bound.addressQuestion(NonUkAddressPage, "livingSettlor.nonUkAddress", Some(controllers.individual.living.routes.NonUkAddressController.onPageLoad(NormalMode).url)),
        bound.yesNoQuestion(PassportDetailsYesNoPage, "livingSettlor.passportDetailsYesNo", Some(controllers.individual.living.routes.PassportDetailsYesNoController.onPageLoad(NormalMode).url)),
        bound.passportDetailsQuestion(PassportDetailsPage, "livingSettlor.passportDetails", Some(controllers.individual.living.routes.PassportDetailsController.onPageLoad(NormalMode).url)),
        bound.yesNoQuestion(IdCardDetailsYesNoPage, "livingSettlor.idCardDetailsYesNo", Some(controllers.individual.living.routes.IdCardDetailsYesNoController.onPageLoad(NormalMode).url)),
        bound.idCardDetailsQuestion(IdCardDetailsPage, "livingSettlor.idCardDetails", Some(controllers.individual.living.routes.IdCardDetailsController.onPageLoad(NormalMode).url)),
        bound.dateQuestion(StartDatePage, "livingSettlor.startDate", Some(controllers.individual.living.routes.StartDateController.onPageLoad().url))
      ).flatten

    val amend: Seq[AnswerRow] = Seq(
      bound.nameQuestion(NamePage, "livingSettlor.name", Some(controllers.individual.living.routes.NameController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(DateOfBirthYesNoPage, "livingSettlor.dateOfBirthYesNo", Some(controllers.individual.living.routes.DateOfBirthYesNoController.onPageLoad(CheckMode).url)),
      bound.dateQuestion(DateOfBirthPage, "livingSettlor.dateOfBirth", Some(controllers.individual.living.routes.DateOfBirthController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "livingSettlor.nationalInsuranceNumberYesNo", Some(controllers.individual.living.routes.NationalInsuranceNumberYesNoController.onPageLoad(CheckMode).url)),
      bound.ninoQuestion(NationalInsuranceNumberPage, "livingSettlor.nationalInsuranceNumber", Some(controllers.individual.living.routes.NationalInsuranceNumberYesNoController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(AddressYesNoPage, "livingSettlor.addressYesNo", Some(controllers.individual.living.routes.AddressYesNoController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(LiveInTheUkYesNoPage, "livingSettlor.liveInTheUkYesNo", Some(controllers.individual.living.routes.LiveInTheUkYesNoController.onPageLoad(CheckMode).url)),
      bound.addressQuestion(UkAddressPage, "livingSettlor.ukAddress", Some(controllers.individual.living.routes.UkAddressController.onPageLoad(CheckMode).url)),
      bound.addressQuestion(NonUkAddressPage, "livingSettlor.nonUkAddress", Some(controllers.individual.living.routes.NonUkAddressController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(PassportDetailsYesNoPage, "livingSettlor.passportDetailsYesNo", Some(controllers.individual.living.routes.PassportDetailsYesNoController.onPageLoad(CheckMode).url)),
      bound.passportDetailsQuestion(PassportDetailsPage, "livingSettlor.passportDetails", Some(controllers.individual.living.routes.PassportDetailsController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(IdCardDetailsYesNoPage, "livingSettlor.idCardDetailsYesNo", Some(controllers.individual.living.routes.IdCardDetailsYesNoController.onPageLoad(CheckMode).url)),
      bound.idCardDetailsQuestion(IdCardDetailsPage, "livingSettlor.idCardDetails", Some(controllers.individual.living.routes.IdCardDetailsController.onPageLoad(CheckMode).url))
    ).flatten

    AnswerSection(
      None,
      if (provisional) add else amend
    )
  }
}
