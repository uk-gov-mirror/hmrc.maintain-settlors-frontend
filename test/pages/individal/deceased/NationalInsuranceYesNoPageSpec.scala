/*
 * Copyright 2021 HM Revenue & Customs
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

package pages.individal.deceased

import java.time.LocalDate

import models.{UkAddress, UserAnswers}
import pages.behaviours.PageBehaviours
import pages.individual.deceased.{AddressYesNoPage, LivedInTheUkYesNoPage, NationalInsuranceNumberPage, NationalInsuranceNumberYesNoPage, UkAddressPage}

class NationalInsuranceYesNoPageSpec extends PageBehaviours {

  "NationalInsuranceYesNoPage" must {

    beRetrievable[Boolean](NationalInsuranceNumberYesNoPage)

    beSettable[Boolean](NationalInsuranceNumberYesNoPage)

    beRemovable[Boolean](NationalInsuranceNumberYesNoPage)

    "implement cleanup logic when YES selected" in {

      val userAnswers = UserAnswers("id", "utr", LocalDate.now, None, None, isDateOfDeathRecorded = true)
        .set(UkAddressPage, UkAddress("line1", "line2", None, None, "postcode")).success.value
        .set(LivedInTheUkYesNoPage, true)
        .flatMap(_.set(AddressYesNoPage, true))
          .flatMap(_.set(NationalInsuranceNumberYesNoPage, true))

      userAnswers.get.get(UkAddressPage) mustNot be(defined)
      userAnswers.get.get(LivedInTheUkYesNoPage) mustNot be(defined)
      userAnswers.get.get(AddressYesNoPage) mustNot be(defined)
    }

    "implement cleanup logic when NO selected" in {
      val userAnswers = UserAnswers("id", "utr", LocalDate.now, None, None, isDateOfDeathRecorded = true)
        .set(NationalInsuranceNumberPage, "nino")
        .flatMap(_.set(NationalInsuranceNumberYesNoPage, false))

      userAnswers.get.get(NationalInsuranceNumberPage) mustNot be(defined)
    }
  }
}
