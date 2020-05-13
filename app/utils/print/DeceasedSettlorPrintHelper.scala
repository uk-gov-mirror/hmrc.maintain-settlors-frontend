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
import models.BpMatchStatus.FullyMatched
import models.UserAnswers
import pages.AdditionalSettlorsYesNoPage
import pages.individual.deceased._
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import viewmodels.AnswerSection

class DeceasedSettlorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter,
                                           countryOptions: CountryOptions
                                            ) {

  def apply(userAnswers: UserAnswers,
            settlorName: String,
            hasAdditionalSettlors: Boolean)(implicit messages: Messages) = {

    val bound = answerRowConverter.bind(userAnswers, settlorName, countryOptions)

    AnswerSection(
      None,
      (userAnswers.get(BpMatchStatusPage) match {
        case Some(FullyMatched) =>
          Seq(
            bound.nameQuestion(NamePage, "deceasedSettlor.name", None),
            bound.yesNoQuestion(DateOfDeathYesNoPage, "deceasedSettlor.dateOfDeathYesNo", if (userAnswers.isDateOfDeathRecorded) None else Some(controllers.individual.deceased.routes.DateOfDeathYesNoController.onPageLoad().url)),
            bound.dateQuestion(DateOfDeathPage, "deceasedSettlor.dateOfDeath", if (userAnswers.isDateOfDeathRecorded) None else Some(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad().url)),
            bound.yesNoQuestion(DateOfBirthYesNoPage, "deceasedSettlor.dateOfBirthYesNo", None),
            bound.dateQuestion(DateOfBirthPage, "deceasedSettlor.dateOfBirth", None),
            bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "deceasedSettlor.nationalInsuranceNumberYesNo", None),
            bound.ninoQuestion(NationalInsuranceNumberPage, "deceasedSettlor.nationalInsuranceNumber", None),
            bound.yesNoQuestion(AddressYesNoPage, "deceasedSettlor.addressYesNo", None),
            bound.yesNoQuestion(LivedInTheUkYesNoPage, "deceasedSettlor.livedInTheUkYesNo", None),
            bound.addressQuestion(UkAddressPage, "deceasedSettlor.ukAddress", None),
            bound.addressQuestion(NonUkAddressPage, "deceasedSettlor.nonUkAddress", None),
            bound.additionalSettlorsQuestion(AdditionalSettlorsYesNoPage, "deceasedSettlor.additionalSettlorsYesNo", Some(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad().url), hasAdditionalSettlors)
          )
        case _ =>
          Seq(
            bound.nameQuestion(NamePage, "deceasedSettlor.name", Some(controllers.individual.deceased.routes.NameController.onPageLoad().url)),
            bound.yesNoQuestion(DateOfDeathYesNoPage, "deceasedSettlor.dateOfDeathYesNo", Some(controllers.individual.deceased.routes.DateOfDeathYesNoController.onPageLoad().url)),
            bound.dateQuestion(DateOfDeathPage, "deceasedSettlor.dateOfDeath", Some(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad().url)),
            bound.yesNoQuestion(DateOfBirthYesNoPage, "deceasedSettlor.dateOfBirthYesNo", Some(controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad().url)),
            bound.dateQuestion(DateOfBirthPage, "deceasedSettlor.dateOfBirth",Some(controllers.individual.deceased.routes.DateOfBirthController.onPageLoad().url)),
            bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "deceasedSettlor.nationalInsuranceNumberYesNo", Some(controllers.individual.deceased.routes.NationalInsuranceNumberYesNoController.onPageLoad().url)),
            bound.ninoQuestion(NationalInsuranceNumberPage, "deceasedSettlor.nationalInsuranceNumber", Some(controllers.individual.deceased.routes.NationalInsuranceNumberController.onPageLoad().url)),
            bound.yesNoQuestion(AddressYesNoPage, "deceasedSettlor.addressYesNo", Some(controllers.individual.deceased.routes.AddressYesNoController.onPageLoad().url)),
            bound.yesNoQuestion(LivedInTheUkYesNoPage, "deceasedSettlor.livedInTheUkYesNo", Some(controllers.individual.deceased.routes.LivedInTheUkYesNoController.onPageLoad().url)),
            bound.addressQuestion(UkAddressPage, "deceasedSettlor.ukAddress", Some(controllers.individual.deceased.routes.UkAddressController.onPageLoad().url)),
            bound.addressQuestion(NonUkAddressPage, "deceasedSettlor.nonUkAddress", Some(controllers.individual.deceased.routes.NonUkAddressController.onPageLoad().url)),
            bound.additionalSettlorsQuestion(AdditionalSettlorsYesNoPage, "deceasedSettlor.additionalSettlorsYesNo", Some(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad().url), hasAdditionalSettlors)
          )
      }).flatten
    )
  }
}
