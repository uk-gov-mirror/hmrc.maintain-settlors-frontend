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
import pages.business._
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection}

class BusinessSettlorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter,
                                           countryOptions: CountryOptions
                                            ) {

  def apply(userAnswers: UserAnswers, provisional: Boolean, settlorName: String)(implicit messages: Messages): AnswerSection = {

    val bound = answerRowConverter.bind(userAnswers, settlorName, countryOptions)

    val add: Seq[AnswerRow] = Seq(
        bound.stringQuestion(NamePage, "businessSettlor.name", Some(controllers.business.routes.NameController.onPageLoad(NormalMode).url)),
        bound.yesNoQuestion(UtrYesNoPage, "businessSettlor.utrYesNo", Some(controllers.business.routes.UtrYesNoController.onPageLoad(NormalMode).url)),
        bound.stringQuestion(UtrPage, "businessSettlor.utr", Some(controllers.business.routes.UtrController.onPageLoad(NormalMode).url)),
        bound.yesNoQuestion(AddressYesNoPage, "businessSettlor.addressYesNo", Some(controllers.business.routes.AddressYesNoController.onPageLoad(NormalMode).url)),
        bound.yesNoQuestion(LiveInTheUkYesNoPage, "businessSettlor.liveInTheUkYesNo", Some(controllers.business.routes.LiveInTheUkYesNoController.onPageLoad(NormalMode).url)),
        bound.addressQuestion(UkAddressPage, "businessSettlor.ukAddress", Some(controllers.business.routes.UkAddressController.onPageLoad(NormalMode).url)),
        bound.addressQuestion(NonUkAddressPage, "businessSettlor.nonUkAddress", Some(controllers.business.routes.NonUkAddressController.onPageLoad(NormalMode).url)),
        bound.companyTypeQuestion(CompanyTypePage, "businessSettlor.companyType", Some(controllers.business.routes.CompanyTypeController.onPageLoad(NormalMode).url)),
        bound.yesNoQuestion(CompanyTimePage, "businessSettlor.companyTime", Some(controllers.business.routes.CompanyTimeController.onPageLoad(NormalMode).url)),
        bound.dateQuestion(StartDatePage, "businessSettlor.startDate", Some(controllers.business.routes.StartDateController.onPageLoad().url))
      ).flatten

    val amend: Seq[AnswerRow] = Seq(
      bound.stringQuestion(NamePage, "businessSettlor.name", Some(controllers.business.routes.NameController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(UtrYesNoPage, "businessSettlor.utrYesNo", Some(controllers.business.routes.UtrYesNoController.onPageLoad(CheckMode).url)),
      bound.stringQuestion(UtrPage, "businessSettlor.utr", Some(controllers.business.routes.UtrController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(AddressYesNoPage, "businessSettlor.addressYesNo", Some(controllers.business.routes.AddressYesNoController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(LiveInTheUkYesNoPage, "businessSettlor.liveInTheUkYesNo", Some(controllers.business.routes.LiveInTheUkYesNoController.onPageLoad(CheckMode).url)),
      bound.addressQuestion(UkAddressPage, "businessSettlor.ukAddress", Some(controllers.business.routes.UkAddressController.onPageLoad(CheckMode).url)),
      bound.addressQuestion(NonUkAddressPage, "businessSettlor.nonUkAddress", Some(controllers.business.routes.NonUkAddressController.onPageLoad(CheckMode).url)),
      bound.companyTypeQuestion(CompanyTypePage, "businessSettlor.companyType", Some(controllers.business.routes.CompanyTypeController.onPageLoad(CheckMode).url)),
      bound.yesNoQuestion(CompanyTimePage, "businessSettlor.companyTime", Some(controllers.business.routes.CompanyTimeController.onPageLoad(CheckMode).url))
    ).flatten

    AnswerSection(
      None,
      if (provisional) add else amend
    )
  }
}
