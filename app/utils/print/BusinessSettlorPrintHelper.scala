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
        bound.stringQuestion(NamePage, "businessSettlor.name", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
        bound.yesNoQuestion(UtrYesNoPage, "businessSettlor.utrYesNo", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
        bound.stringQuestion(UtrPage, "businessSettlor.utr", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
        bound.yesNoQuestion(AddressYesNoPage, "businessSettlor.addressYesNo", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
        bound.yesNoQuestion(LiveInTheUkYesNoPage, "businessSettlor.liveInTheUkYesNo", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
        bound.addressQuestion(UkAddressPage, "businessSettlor.ukAddress", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
        bound.addressQuestion(NonUkAddressPage, "businessSettlor.nonUkAddress", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
        bound.companyTypeQuestion(CompanyTypePage, "businessSettlor.companyType", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
        bound.yesNoQuestion(CompanyTimePage, "businessSettlor.companyTime", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
        bound.dateQuestion(StartDatePage, "businessSettlor.startDate", controllers.routes.FeatureNotAvailableController.onPageLoad().url)
      ).flatten

    val amend: Seq[AnswerRow] = Seq(
      bound.stringQuestion(NamePage, "businessSettlor.name", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
      bound.yesNoQuestion(UtrYesNoPage, "businessSettlor.utrYesNo", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
      bound.stringQuestion(UtrPage, "businessSettlor.utr", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
      bound.yesNoQuestion(AddressYesNoPage, "businessSettlor.addressYesNo", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
      bound.yesNoQuestion(LiveInTheUkYesNoPage, "businessSettlor.liveInTheUkYesNo", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
      bound.addressQuestion(UkAddressPage, "businessSettlor.ukAddress", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
      bound.addressQuestion(NonUkAddressPage, "businessSettlor.nonUkAddress", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
      bound.companyTypeQuestion(CompanyTypePage, "businessSettlor.companyType", controllers.routes.FeatureNotAvailableController.onPageLoad().url),
      bound.yesNoQuestion(CompanyTimePage, "businessSettlor.companyTime", controllers.routes.FeatureNotAvailableController.onPageLoad().url)
    ).flatten

    AnswerSection(
      None,
      if (provisional) add else amend
    )
  }
}
