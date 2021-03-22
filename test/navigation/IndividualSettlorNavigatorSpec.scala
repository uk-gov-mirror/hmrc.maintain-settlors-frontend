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

package navigation

import base.SpecBase
import controllers.individual.living.add.{routes => addRts}
import controllers.individual.living.amend.{routes => amendRts}
import controllers.individual.living.{routes => rts}
import models._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.individual.living._

class IndividualSettlorNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks  {

  val navigator = new IndividualSettlorNavigator

  "Individual settlor navigator" when {
    
    "adding" must {
      
      val mode: Mode = NormalMode

      "Name page -> Do you know date of birth page" in {
        navigator.nextPage(NamePage, mode, emptyUserAnswers)
          .mustBe(rts.DateOfBirthYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> Yes -> Date of birth page" in {
        val answers = emptyUserAnswers
          .set(DateOfBirthYesNoPage, true).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.DateOfBirthController.onPageLoad(mode))
      }

      "Date of birth page -> Do you know NINO page" in {
        navigator.nextPage(DateOfBirthPage, mode, emptyUserAnswers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> No -> Do you know NINO page" in {
        val answers = emptyUserAnswers
          .set(DateOfBirthYesNoPage, false).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Do you know NINO page -> Yes -> NINO page" in {
        val answers = emptyUserAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value

        navigator.nextPage(NationalInsuranceNumberYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberController.onPageLoad(mode))
      }

      "NINO page -> Start Date page" in {
        val answers = emptyUserAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value

        navigator.nextPage(NationalInsuranceNumberPage, mode, answers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Do you know NINO page -> No -> Do you know address page" in {
        val answers = emptyUserAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value

        navigator.nextPage(NationalInsuranceNumberYesNoPage, mode, answers)
          .mustBe(rts.AddressYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> Yes -> Is address in UK page" in {
        val answers = emptyUserAnswers
          .set(AddressYesNoPage, true).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(rts.LiveInTheUkYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> No -> Start Date page" in {
        val answers = emptyUserAnswers
          .set(AddressYesNoPage, false).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Is address in UK page -> Yes -> UK address page" in {
        val answers = emptyUserAnswers
          .set(LiveInTheUkYesNoPage, true).success.value

        navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
          .mustBe(rts.UkAddressController.onPageLoad(mode))
      }

      "UK address page -> Do you know passport details page" in {
        navigator.nextPage(UkAddressPage, mode, emptyUserAnswers)
          .mustBe(addRts.PassportDetailsYesNoController.onPageLoad())
      }

      "Is address in UK page -> No -> Non-UK address page" in {
        val answers = emptyUserAnswers
          .set(LiveInTheUkYesNoPage, false).success.value

        navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
          .mustBe(rts.NonUkAddressController.onPageLoad(mode))
      }

      "Non-UK address page -> Do you know passport details page" in {
        navigator.nextPage(NonUkAddressPage, mode, emptyUserAnswers)
          .mustBe(addRts.PassportDetailsYesNoController.onPageLoad())
      }

      "Do you know passport details page -> Yes -> Passport details page" in {
        val answers = emptyUserAnswers
          .set(PassportDetailsYesNoPage, true).success.value

        navigator.nextPage(PassportDetailsYesNoPage, mode, answers)
          .mustBe(addRts.PassportDetailsController.onPageLoad())
      }

      "Passport details page -> Start Date page" in {
        navigator.nextPage(PassportDetailsPage, mode, emptyUserAnswers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Do you know passport details page -> No -> Do you know ID card details page" in {
        val answers = emptyUserAnswers
          .set(PassportDetailsYesNoPage, false).success.value

        navigator.nextPage(PassportDetailsYesNoPage, mode, answers)
          .mustBe(addRts.IdCardDetailsYesNoController.onPageLoad())
      }

      "Do you know ID card details page -> Yes -> ID card details page" in {
        val answers = emptyUserAnswers
          .set(IdCardDetailsYesNoPage, true).success.value

        navigator.nextPage(IdCardDetailsYesNoPage, mode, answers)
          .mustBe(addRts.IdCardDetailsController.onPageLoad())
      }

      "ID card details page -> Start Date page" in {
        navigator.nextPage(IdCardDetailsPage, mode, emptyUserAnswers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Do you know ID card details page -> No -> Start Date page" in {
        val answers = emptyUserAnswers
          .set(IdCardDetailsYesNoPage, false).success.value

        navigator.nextPage(IdCardDetailsYesNoPage, mode, answers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Start Date page -> Check details" in {
        navigator.nextPage(StartDatePage, mode, emptyUserAnswers)
          .mustBe(addRts.CheckDetailsController.onPageLoad())
      }
    }
    
    "amending" must {

      val mode: Mode = CheckMode
      val index: Int = 0
      val baseAnswers: UserAnswers = emptyUserAnswers.set(IndexPage, index).success.value

      "Name page -> Do you know date of birth page" in {
        navigator.nextPage(NamePage, mode, baseAnswers)
          .mustBe(rts.DateOfBirthYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> Yes -> Date of birth page" in {
        val answers = baseAnswers
          .set(DateOfBirthYesNoPage, true).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.DateOfBirthController.onPageLoad(mode))
      }

      "Date of birth page -> Do you know NINO page" in {
        navigator.nextPage(DateOfBirthPage, mode, baseAnswers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> No -> Do you know NINO page" in {
        val answers = baseAnswers
          .set(DateOfBirthYesNoPage, false).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Do you know NINO page -> Yes -> NINO page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value

        navigator.nextPage(NationalInsuranceNumberYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberController.onPageLoad(mode))
      }

      "NINO page -> Check details" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value

        navigator.nextPage(NationalInsuranceNumberPage, mode, answers)
          .mustBe(controllers.individual.living.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
      }

      "Do you know NINO page -> No -> Do you know address page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value

        navigator.nextPage(NationalInsuranceNumberYesNoPage, mode, answers)
          .mustBe(rts.AddressYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> Yes -> Is address in UK page" in {
        val answers = baseAnswers
          .set(AddressYesNoPage, true).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(rts.LiveInTheUkYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> No -> Check details page" in {
        val answers = baseAnswers
          .set(AddressYesNoPage, false).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(amendRts.CheckDetailsController.renderFromUserAnswers(index))
      }

      "Is address in UK page -> Yes -> UK address page" in {
        val answers = baseAnswers
          .set(LiveInTheUkYesNoPage, true).success.value

        navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
          .mustBe(rts.UkAddressController.onPageLoad(mode))
      }

      "UK address page -> Do you know passport or ID card details page" in {
        navigator.nextPage(UkAddressPage, mode, baseAnswers)
          .mustBe(amendRts.PassportOrIdCardDetailsYesNoController.onPageLoad())
      }

      "Is address in UK page -> No -> Non-UK address page" in {
        val answers = baseAnswers
          .set(LiveInTheUkYesNoPage, false).success.value

        navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
          .mustBe(rts.NonUkAddressController.onPageLoad(mode))
      }

      "Non-UK address page -> Do you know passport or ID card details page" in {
        navigator.nextPage(NonUkAddressPage, mode, baseAnswers)
          .mustBe(amendRts.PassportOrIdCardDetailsYesNoController.onPageLoad())
      }

      "Do you know passport or ID card details page -> Yes -> Passport or ID card details page" in {
        val answers = baseAnswers
          .set(PassportOrIdCardDetailsYesNoPage, true).success.value

        navigator.nextPage(PassportOrIdCardDetailsYesNoPage, mode, answers)
          .mustBe(amendRts.PassportOrIdCardDetailsController.onPageLoad())
      }

      "Passport or ID card details page -> Check details" in {
        navigator.nextPage(PassportOrIdCardDetailsPage, mode, baseAnswers)
          .mustBe(amendRts.CheckDetailsController.renderFromUserAnswers(index))
      }

      "Do you know passport or ID card details page -> No -> Check details" in {
        val answers = baseAnswers
          .set(PassportOrIdCardDetailsYesNoPage, false).success.value

        navigator.nextPage(PassportOrIdCardDetailsYesNoPage, mode, answers)
          .mustBe(amendRts.CheckDetailsController.renderFromUserAnswers(index))
      }
    }
  }
}
