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

import models.UserAnswers
import pages.behaviours.PageBehaviours
import pages.individual.deceased.{DateOfDeathPage, DateOfDeathYesNoPage}


class DateOfDeathYesNoPageSpec extends PageBehaviours {

  "DateOfDeathYesNoPage" must {

    beRetrievable[Boolean](DateOfDeathYesNoPage)

    beSettable[Boolean](DateOfDeathYesNoPage)

    beRemovable[Boolean](DateOfDeathYesNoPage)

    "implement cleanup logic when NO selected" in {
      val userAnswers = UserAnswers("id", "utr", LocalDate.now, None, None, isDateOfDeathRecorded = true)
        .set(DateOfDeathPage, LocalDate.now)
        .flatMap(_.set(DateOfDeathYesNoPage, false))

      userAnswers.get.get(DateOfDeathPage) mustNot be(defined)
    }
  }
}
