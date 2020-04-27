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
import pages.individual.living._
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import viewmodels.AnswerSection

class LivingSettlorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter,
                                                 countryOptions: CountryOptions
                                            ) {

  def apply(userAnswers: UserAnswers, beneficiaryName: String)(implicit messages: Messages) = {

    val bound = answerRowConverter.bind(userAnswers, beneficiaryName, countryOptions)

    AnswerSection(
      None,
      Seq(
        bound.nameQuestion(NamePage, "livingSettlor.name", controllers.individual.living.routes.NameController.onPageLoad().url),
        bound.yesNoQuestion(DateOfBirthYesNoPage, "livingSettlor.dateOfBirthYesNo", controllers.individual.living.routes.DateOfBirthYesNoController.onPageLoad().url),
        bound.dateQuestion(DateOfBirthPage, "livingSettlor.dateOfBirth", controllers.individual.living.routes.DateOfBirthController.onPageLoad().url),
        bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "livingSettlor.nationalInsuranceNumberYesNo", controllers.individual.living.routes.NationalInsuranceNumberYesNoController.onPageLoad().url),
        bound.ninoQuestion(NationalInsuranceNumberPage, "livingSettlor.nationalInsuranceNumber", controllers.individual.living.routes.NationalInsuranceNumberYesNoController.onPageLoad().url),
        bound.yesNoQuestion(AddressYesNoPage, "livingSettlor.addressYesNo", controllers.individual.living.routes.AddressYesNoController.onPageLoad().url),
        bound.yesNoQuestion(LiveInTheUkYesNoPage, "livingSettlor.liveInTheUkYesNo", controllers.individual.living.routes.LiveInTheUkYesNoController.onPageLoad().url),
        bound.addressQuestion(UkAddressPage, "livingSettlor.ukAddress", controllers.individual.living.routes.UkAddressController.onPageLoad().url),
        bound.addressQuestion(NonUkAddressPage, "livingSettlor.nonUkAddress", controllers.individual.living.routes.NonUkAddressController.onPageLoad().url),
        bound.yesNoQuestion(PassportDetailsYesNoPage, "livingSettlor.passportDetailsYesNo", controllers.individual.living.routes.PassportDetailsYesNoController.onPageLoad().url),
        bound.passportDetailsQuestion(PassportDetailsPage, "livingSettlor.passportDetails", controllers.individual.living.routes.PassportDetailsController.onPageLoad().url),
        bound.yesNoQuestion(IdCardDetailsYesNoPage, "livingSettlor.idCardDetailsYesNo", controllers.individual.living.routes.IdCardDetailsYesNoController.onPageLoad().url),
        bound.idCardDetailsQuestion(IdCardDetailsPage, "livingSettlor.idCardDetails", controllers.individual.living.routes.IdCardDetailsController.onPageLoad().url),
        bound.dateQuestion(StartDatePage, "livingSettlor.startDate", controllers.individual.living.routes.StartDateController.onPageLoad().url)
      ).flatten
    )
  }
}
