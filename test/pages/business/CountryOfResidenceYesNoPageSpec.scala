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

package pages.business

import java.time.LocalDate

import models.{TypeOfTrust, UkAddress, UserAnswers}
import pages.behaviours.PageBehaviours

class CountryOfResidenceYesNoPageSpec extends PageBehaviours {

  "CountryOfResidenceYesNoPage" must {

    beRetrievable[Boolean](CountryOfResidenceYesNoPage)

    beSettable[Boolean](CountryOfResidenceYesNoPage)

    beRemovable[Boolean](CountryOfResidenceYesNoPage)

    "implement cleanup logic when NO selected" in {
      val userAnswers = UserAnswers("id", "identifier", LocalDate.now, TypeOfTrust.WillTrustOrIntestacyTrust, None, isDateOfDeathRecorded = true)
        .set(CountryOfResidencePage, "FR").success.value
        .set(CountryOfResidenceInTheUkYesNoPage, false).success.value
        .set(CountryOfResidencePage, "FR").success.value
        .set(AddressYesNoPage, false).success.value
        .set(UkAddressPage, UkAddress("line1", "line2", None, None, "postcode")).success.value
        .set(LiveInTheUkYesNoPage, false).success.value

      val result = userAnswers.set(CountryOfResidenceYesNoPage, false).success.value

      result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
      result.get(CountryOfResidencePage) mustBe None
      result.get(AddressYesNoPage) mustBe None
      result.get(LiveInTheUkYesNoPage) mustBe None
      result.get(UkAddressPage) mustBe None
    }
  }
}
