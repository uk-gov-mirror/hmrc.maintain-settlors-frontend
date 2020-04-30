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
import models.UserAnswers
import pages.individual.deceased._
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import viewmodels.AnswerSection

class DeceasedSettlorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter,
                                           countryOptions: CountryOptions
                                            ) {

  def apply(userAnswers: UserAnswers, settlorName: String)(implicit messages: Messages) = {

    val bound = answerRowConverter.bind(userAnswers, settlorName, countryOptions)

    AnswerSection(
      None,
      Seq(
        bound.nameQuestion(NamePage, "deceasedSettlor.name", controllers.individual.deceased.routes.NameController.onPageLoad().url),
        bound.yesNoQuestion(DateOfDeathYesNoPage, "deceasedSettlor.dateOfDeathYesNo", controllers.individual.deceased.routes.DateOfDeathYesNoController.onPageLoad().url),
        bound.dateQuestion(DateOfDeathPage, "deceasedSettlor.dateOfDeath", controllers.individual.deceased.routes.DateOfDeathController.onPageLoad().url),
        bound.yesNoQuestion(DateOfBirthYesNoPage, "deceasedSettlor.dateOfBirthYesNo", controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad().url),
        bound.dateQuestion(DateOfBirthPage, "deceasedSettlor.dateOfBirth", controllers.individual.deceased.routes.DateOfBirthController.onPageLoad().url),
        bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "deceasedSettlor.nationalInsuranceNumberYesNo", controllers.individual.deceased.routes.NationalInsuranceNumberYesNoController.onPageLoad().url),
        bound.ninoQuestion(NationalInsuranceNumberPage, "deceasedSettlor.nationalInsuranceNumber", controllers.individual.deceased.routes.NationalInsuranceNumberYesNoController.onPageLoad().url),
        bound.yesNoQuestion(AddressYesNoPage, "deceasedSettlor.addressYesNo", controllers.individual.deceased.routes.AddressYesNoController.onPageLoad().url),
        bound.yesNoQuestion(LivedInTheUkYesNoPage, "deceasedSettlor.liveInTheUkYesNo", controllers.individual.deceased.routes.LivedInTheUkYesNoController.onPageLoad().url),
        bound.addressQuestion(UkAddressPage, "deceasedSettlor.ukAddress", controllers.individual.deceased.routes.UkAddressController.onPageLoad().url),
        bound.addressQuestion(NonUkAddressPage, "deceasedSettlor.nonUkAddress", controllers.individual.deceased.routes.NonUkAddressController.onPageLoad().url)
      ).flatten
    )
  }
}
