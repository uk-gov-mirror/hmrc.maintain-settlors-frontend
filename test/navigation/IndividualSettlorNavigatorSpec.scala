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

package navigation

import java.time.LocalDate

import base.SpecBase
import models.{NormalMode, UserAnswers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.individual.living._

class IndividualSettlorNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks  {

  val navigator = new IndividualSettlorNavigator

  "Individual settlor navigator" when {

      "Name page -> Do you know date of birth page" in {
        navigator.nextPage(NamePage, emptyUserAnswers)
          .mustBe(controllers.individual.living.routes.DateOfBirthYesNoController.onPageLoad(NormalMode))
      }

    "Do you know date of birth page -> Yes -> Date of birth page" in {
      val answers = emptyUserAnswers
        .set(DateOfBirthYesNoPage, true).success.value

      navigator.nextPage(DateOfBirthYesNoPage, answers)
        .mustBe(controllers.individual.living.routes.DateOfBirthController.onPageLoad(NormalMode))
    }

    "Date of birth page -> Do you know NINO page" in {
      navigator.nextPage(DateOfBirthPage, emptyUserAnswers)
        .mustBe(controllers.individual.living.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode))
    }

    "Do you know date of birth page -> No -> Do you know NINO page" in {
      val answers = emptyUserAnswers
        .set(DateOfBirthYesNoPage, false).success.value

      navigator.nextPage(DateOfBirthYesNoPage, answers)
        .mustBe(controllers.individual.living.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode))
    }

    "Do you know NINO page -> Yes -> NINO page" in {
      val answers = emptyUserAnswers
        .set(NationalInsuranceNumberYesNoPage, true).success.value

      navigator.nextPage(NationalInsuranceNumberYesNoPage, answers)
        .mustBe(controllers.individual.living.routes.NationalInsuranceNumberController.onPageLoad(NormalMode))
    }

    "NINO page -> Start Date page" in {
      navigator.nextPage(NationalInsuranceNumberPage, emptyUserAnswers)
        .mustBe(controllers.individual.living.routes.StartDateController.onPageLoad())
    }

    "Do you know NINO page -> No -> Do you know address page" in {
      val answers = emptyUserAnswers
        .set(NationalInsuranceNumberYesNoPage, false).success.value

      navigator.nextPage(NationalInsuranceNumberYesNoPage, answers)
        .mustBe(controllers.individual.living.routes.AddressYesNoController.onPageLoad(NormalMode))
    }

    "Do you know address page -> Yes -> Is address in UK page" in {
      val answers = emptyUserAnswers
        .set(AddressYesNoPage, true).success.value

      navigator.nextPage(AddressYesNoPage, answers)
        .mustBe(controllers.individual.living.routes.LiveInTheUkYesNoController.onPageLoad(NormalMode))
    }

    "Do you know address page -> No -> Start Date page" in {
      val answers = emptyUserAnswers
        .set(AddressYesNoPage, false).success.value

      navigator.nextPage(AddressYesNoPage, answers)
        .mustBe(controllers.individual.living.routes.StartDateController.onPageLoad())
    }

    "Is address in UK page -> Yes -> UK address page" in {
      val answers = emptyUserAnswers
        .set(LiveInTheUkYesNoPage, true).success.value

      navigator.nextPage(LiveInTheUkYesNoPage, answers)
        .mustBe(controllers.individual.living.routes.UkAddressController.onPageLoad(NormalMode))
    }

    "UK address page -> Do you know passport details page" in {
      navigator.nextPage(UkAddressPage, emptyUserAnswers)
        .mustBe(controllers.individual.living.routes.PassportDetailsYesNoController.onPageLoad(NormalMode))
    }

    "Is address in UK page -> No -> Non-UK address page" in {
      val answers = emptyUserAnswers
        .set(LiveInTheUkYesNoPage, false).success.value

      navigator.nextPage(LiveInTheUkYesNoPage, answers)
        .mustBe(controllers.individual.living.routes.NonUkAddressController.onPageLoad(NormalMode))
    }

    "Non-UK address page -> Do you know passport details page" in {
      navigator.nextPage(NonUkAddressPage, emptyUserAnswers)
        .mustBe(controllers.individual.living.routes.PassportDetailsYesNoController.onPageLoad(NormalMode))
    }

    "Do you know passport details page -> Yes -> Passport details page" in {
      val answers = emptyUserAnswers
        .set(PassportDetailsYesNoPage, true).success.value

      navigator.nextPage(PassportDetailsYesNoPage, answers)
        .mustBe(controllers.individual.living.routes.PassportDetailsController.onPageLoad(NormalMode))
    }

    "Passport details page -> Start Date page" in {
      navigator.nextPage(PassportDetailsPage, emptyUserAnswers)
        .mustBe(controllers.individual.living.routes.StartDateController.onPageLoad())
    }

    "Do you know passport details page -> No -> Do you know ID card details page" in {
      val answers = emptyUserAnswers
        .set(PassportDetailsYesNoPage, false).success.value

      navigator.nextPage(PassportDetailsYesNoPage, answers)
        .mustBe(controllers.individual.living.routes.IdCardDetailsYesNoController.onPageLoad(NormalMode))
    }

    "Do you know ID card details page -> Yes -> ID card details page" in {
      val answers = emptyUserAnswers
        .set(IdCardDetailsYesNoPage, true).success.value

      navigator.nextPage(IdCardDetailsYesNoPage, answers)
        .mustBe(controllers.individual.living.routes.IdCardDetailsController.onPageLoad(NormalMode))
    }

    "ID card details page -> Start Date page" in {
      navigator.nextPage(IdCardDetailsPage, emptyUserAnswers)
        .mustBe(controllers.individual.living.routes.StartDateController.onPageLoad())
    }

    "Do you know ID card details page -> No -> Start Date page" in {
      val answers = emptyUserAnswers
        .set(IdCardDetailsYesNoPage, false).success.value

      navigator.nextPage(IdCardDetailsYesNoPage, answers)
        .mustBe(controllers.individual.living.routes.StartDateController.onPageLoad())
    }
  }
}
