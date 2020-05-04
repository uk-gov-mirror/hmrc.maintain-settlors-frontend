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

import base.SpecBase
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.individual.deceased._

class DeceasedSettlorNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks  {

  val navigator = new DeceasedSettlorNavigator

  "deceased settlor navigator" when {

    "Name page -> Do you know date of death page" in {
      navigator.nextPage(NamePage, emptyUserAnswers)
        .mustBe(controllers.individual.deceased.routes.DateOfDeathYesNoController.onPageLoad())
    }

    "Do you know date of death page -> Yes -> Date of death page" in {
      val answers = emptyUserAnswers
        .set(DateOfDeathYesNoPage, true).success.value

      navigator.nextPage(DateOfDeathYesNoPage, answers)
        .mustBe(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad())
    }

    "Date of death page -> Do you know Date of birth page" in {
      navigator.nextPage(DateOfDeathPage, emptyUserAnswers)
        .mustBe(controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad())
    }

    "Do you know date of death page -> No -> Do you know Date of birth page" in {
      val answers = emptyUserAnswers
        .set(DateOfDeathYesNoPage, false).success.value

      navigator.nextPage(DateOfDeathYesNoPage, answers)
        .mustBe(controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad())
    }

    "Do you know date of birth page -> Yes -> Date of birth page" in {
      val answers = emptyUserAnswers
        .set(DateOfBirthYesNoPage, true).success.value

      navigator.nextPage(DateOfBirthYesNoPage, answers)
        .mustBe(controllers.individual.deceased.routes.DateOfBirthController.onPageLoad())
    }

    "Date of birth page -> Do you know NINO page" in {
      navigator.nextPage(DateOfBirthPage, emptyUserAnswers)
        .mustBe(controllers.individual.deceased.routes.NationalInsuranceNumberYesNoController.onPageLoad())
    }

    "Do you know date of birth page -> No -> Do you know NINO page" in {
      val answers = emptyUserAnswers
        .set(DateOfBirthYesNoPage, false).success.value

      navigator.nextPage(DateOfBirthYesNoPage, answers)
        .mustBe(controllers.individual.deceased.routes.NationalInsuranceNumberYesNoController.onPageLoad())
    }

    "Do you know NINO page -> Yes -> NINO page" in {
      val answers = emptyUserAnswers
        .set(NationalInsuranceNumberYesNoPage, true).success.value

      navigator.nextPage(NationalInsuranceNumberYesNoPage, answers)
        .mustBe(controllers.individual.deceased.routes.NationalInsuranceNumberController.onPageLoad())
    }

    "NINO page -> Check Details page" in {
      val answers = emptyUserAnswers
        .set(IndexPage, 0).success.value

      navigator.nextPage(NationalInsuranceNumberPage, answers)
        .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
    }

    "Do you know NINO page -> No -> Do you know address page" in {
      val answers = emptyUserAnswers
        .set(NationalInsuranceNumberYesNoPage, false).success.value

      navigator.nextPage(NationalInsuranceNumberYesNoPage, answers)
        .mustBe(controllers.individual.deceased.routes.AddressYesNoController.onPageLoad())
    }

    "Do you know address page -> Yes -> Was address in UK page" in {
      val answers = emptyUserAnswers
        .set(AddressYesNoPage, true).success.value

      navigator.nextPage(AddressYesNoPage, answers)
        .mustBe(controllers.individual.deceased.routes.LivedInTheUkYesNoController.onPageLoad())
    }

    "Do you know address page -> No -> Check Details page" in {
      val answers = emptyUserAnswers
        .set(AddressYesNoPage, false).success.value
        .set(IndexPage, 0).success.value

      navigator.nextPage(AddressYesNoPage, answers)
        .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
    }

    "Is address in UK page -> Yes -> UK address page" in {
      val answers = emptyUserAnswers
        .set(LivedInTheUkYesNoPage, true).success.value

      navigator.nextPage(LivedInTheUkYesNoPage, answers)
        .mustBe(controllers.individual.deceased.routes.UkAddressController.onPageLoad())
    }

    "UK address page -> Check Details page" in {
      val answers = emptyUserAnswers
        .set(IndexPage, 0).success.value

      navigator.nextPage(UkAddressPage, answers)
        .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
    }

    "Is address in UK page -> No -> Non-UK address page" in {
      val answers = emptyUserAnswers
        .set(LivedInTheUkYesNoPage, false).success.value

      navigator.nextPage(LivedInTheUkYesNoPage, answers)
        .mustBe(controllers.individual.deceased.routes.NonUkAddressController.onPageLoad())
    }

    "Non-UK address page -> Check Details page" in {
      val answers = emptyUserAnswers
        .set(IndexPage, 0).success.value

      navigator.nextPage(NonUkAddressPage, answers)
        .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
    }
  }
}
