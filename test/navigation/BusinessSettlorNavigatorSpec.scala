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
import models.{CheckMode, NormalMode, TypeOfTrust}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.business._

class BusinessSettlorNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks  {

  val navigator = new BusinessSettlorNavigator

  "Business settlor navigator" when {

    "4mld" must {
      val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = false)
      "add journey navigation" must {

        val mode = NormalMode

        "Name page -> Do you know UTR page" in {
          navigator.nextPage(NamePage, mode, baseAnswers)
            .mustBe(controllers.business.routes.UtrYesNoController.onPageLoad(mode))
        }

        "Do you know UTR page -> Yes -> UTR page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, true).success.value

          navigator.nextPage(UtrYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.UtrController.onPageLoad(mode))
        }

        "UTR page -> (Employee-related trust) -> Company type page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, true).success.value

          navigator.nextPage(UtrPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "UTR page -> (Non-employee-related trust) -> Start date page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, true).success.value

          navigator.nextPage(UtrPage, mode, answers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Do you know UTR page -> No -> Do you know address page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, false).success.value

          navigator.nextPage(UtrYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
        }

        "Do you know address page -> Yes -> Is address in UK page" in {
          val answers = baseAnswers
            .set(AddressYesNoPage, true).success.value

          navigator.nextPage(AddressYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.LiveInTheUkYesNoController.onPageLoad(mode))
        }

        "Do you know address page -> No (Employee-related trust) -> Company Type page" in {
          val answers = baseAnswers
            .set(AddressYesNoPage, false).success.value

          navigator.nextPage(AddressYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "Do you know address page -> No (Non-employee-related trust) -> Start Date page" in {
          val answers = baseAnswers
            .set(AddressYesNoPage, false).success.value

          navigator.nextPage(AddressYesNoPage, mode, answers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Is address in UK page -> Yes -> UK address page" in {
          val answers = baseAnswers
            .set(LiveInTheUkYesNoPage, true).success.value

          navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.UkAddressController.onPageLoad(mode))
        }

        "Is address in UK page -> No -> Non-UK address page" in {
          val answers = baseAnswers
            .set(LiveInTheUkYesNoPage, false).success.value

          navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.NonUkAddressController.onPageLoad(mode))
        }

        "UK address page -> (Employee-related trust) -> Company type page" in {
          navigator.nextPage(UkAddressPage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "UK address page -> (Non-employee-related trust) -> Start date page" in {
          navigator.nextPage(UkAddressPage, mode, baseAnswers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Non-UK address page -> (Employee-related trust) -> Company type page" in {
          navigator.nextPage(NonUkAddressPage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "Non-UK address page -> (Non-employee-related trust) -> Start date page" in {
          navigator.nextPage(NonUkAddressPage, mode, baseAnswers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Company type page -> Company time page" in {
          navigator.nextPage(CompanyTypePage, mode, baseAnswers)
            .mustBe(controllers.business.routes.CompanyTimeController.onPageLoad(mode))
        }

        "Company time page -> Start date page" in {
          navigator.nextPage(CompanyTimePage, mode, baseAnswers)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Start date page -> Check details page" in {
          navigator.nextPage(StartDatePage, mode, baseAnswers)
            .mustBe(controllers.business.add.routes.CheckDetailsController.onPageLoad())
        }
      }

      "amend journey navigation" must {

        val mode = CheckMode
        val index = 0

        "Name page -> Do you know UTR page" in {
          navigator.nextPage(NamePage, mode, baseAnswers)
            .mustBe(controllers.business.routes.UtrYesNoController.onPageLoad(mode))
        }

        "Do you know UTR page -> Yes -> UTR page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, true).success.value

          navigator.nextPage(UtrYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.UtrController.onPageLoad(mode))
        }

        "UTR page -> (Employee-related trust) -> Company type page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, true).success.value

          navigator.nextPage(UtrPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "UTR page -> (Non-employee-related trust) -> Check details page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, true).success.value
            .set(IndexPage, index).success.value

          navigator.nextPage(UtrPage, mode, answers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }

        "Do you know UTR page -> No -> Do you know address page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, false).success.value

          navigator.nextPage(UtrYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
        }

        "Do you know address page -> Yes -> Is address in UK page" in {
          val answers = baseAnswers
            .set(AddressYesNoPage, true).success.value

          navigator.nextPage(AddressYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.LiveInTheUkYesNoController.onPageLoad(mode))
        }

        "Do you know address page -> No -> Check details page" in {
          val answers = baseAnswers
            .set(IndexPage, index).success.value
            .set(AddressYesNoPage, false).success.value

          navigator.nextPage(AddressYesNoPage, mode, answers)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }

        "Is address in UK page -> Yes -> UK address page" in {
          val answers = baseAnswers
            .set(LiveInTheUkYesNoPage, true).success.value

          navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.UkAddressController.onPageLoad(mode))
        }

        "Is address in UK page -> No -> Non-UK address page" in {
          val answers = baseAnswers
            .set(LiveInTheUkYesNoPage, false).success.value

          navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.NonUkAddressController.onPageLoad(mode))
        }

        "UK address page -> (Employee-related trust) -> Company type page" in {
          navigator.nextPage(UkAddressPage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "UK address page -> (Non-employee-related trust) -> Check details page" in {
          val answers = baseAnswers
            .set(IndexPage, index).success.value

          navigator.nextPage(UkAddressPage, mode, answers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }

        "Non-UK address page -> (Employee-related trust) -> Company type page" in {
          navigator.nextPage(NonUkAddressPage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "Non-UK address page -> (Non-employee-related trust) -> Check details page" in {
          val answers = baseAnswers
            .set(IndexPage, index).success.value

          navigator.nextPage(NonUkAddressPage, mode, answers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }

        "Company type page -> Company time page" in {
          navigator.nextPage(CompanyTypePage, mode, baseAnswers)
            .mustBe(controllers.business.routes.CompanyTimeController.onPageLoad(mode))
        }

        "Company time page -> Check details page" in {
          val answers = baseAnswers
            .set(IndexPage, index).success.value

          navigator.nextPage(CompanyTimePage, mode, answers)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }
      }
    }

    "5mld taxable" must {

      "add journey navigation" must {
        val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true)
        val mode = NormalMode

        "Name page -> Do you know UTR page" in {
          navigator.nextPage(NamePage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.UtrYesNoController.onPageLoad(mode))
        }

        "Do you know UTR page -> Yes -> UTR page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, true).success.value

          navigator.nextPage(UtrYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.UtrController.onPageLoad(mode))
        }

        "UTR page -> Do you know the country of residence" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, true).success.value

          navigator.nextPage(UtrPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "Do you know UTR page -> No -> Do you country of residence" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, false).success.value

          navigator.nextPage(UtrYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "Do you know the country of residence -> Yes -> Is residence in UK page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, true).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CountryOfResidenceInTheUkYesNoController.onPageLoad(mode))
        }

        "Do you know the country of residence -> No (Employee-related trust) -> Company type page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "Do you know the country of residence -> No (Non-employee-related trust) -> Start date page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }


        "Is residence in UK page-> Yes -> Do you know address" in {
          val answers = baseAnswers
            .set(CountryOfResidenceInTheUkYesNoPage, true).success.value

          navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
        }

        "Is residence in UK page -> No -> Country of Residence page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceInTheUkYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CountryOfResidenceController.onPageLoad(mode))
        }

        "Country of Residence page -> No Utr -> Do you know address page" in {
          val answers = baseAnswers
            .set(CountryOfResidencePage, "ES").success.value
            .set(UtrYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidencePage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
        }

        "Country of Residence page -> With Utr (Employee-related trust) -> Company type page" in {
          val answers = baseAnswers
            .set(CountryOfResidencePage, "ES").success.value
            .set(UtrYesNoPage, true).success.value
            .set(UtrPage, "12345678").success.value

          navigator.nextPage(CountryOfResidencePage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "Country of Residence page -> With Utr (Non-employee-related trust) -> Start date page" in {
          val answers = baseAnswers
            .set(CountryOfResidencePage, "ES").success.value
            .set(UtrYesNoPage, true).success.value
            .set(UtrPage, "12345678").success.value

          navigator.nextPage(CountryOfResidencePage, mode, answers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Do you know address page -> Yes -> Is address in UK page" in {
          val answers = baseAnswers
            .set(AddressYesNoPage, true).success.value

          navigator.nextPage(AddressYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.LiveInTheUkYesNoController.onPageLoad(mode))
        }

        "Do you know address page -> No (Employee-related trust) -> Company type page" in {
          val answers = baseAnswers
            .set(AddressYesNoPage, false).success.value

          navigator.nextPage(AddressYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "Do you know address page -> No (Non-employee-related trust) -> Start date page" in {
          val answers = baseAnswers
            .set(AddressYesNoPage, false).success.value

          navigator.nextPage(AddressYesNoPage, mode, answers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Is address in UK page -> Yes -> UK address page" in {
          val answers = baseAnswers
            .set(LiveInTheUkYesNoPage, true).success.value

          navigator.nextPage(LiveInTheUkYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.UkAddressController.onPageLoad(mode))
        }

        "Is address in UK page -> No -> Non-UK address page" in {
          val answers = baseAnswers
            .set(LiveInTheUkYesNoPage, false).success.value

          navigator.nextPage(LiveInTheUkYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.NonUkAddressController.onPageLoad(mode))
        }

        "UK address page -> (Employee-related trust) -> Company type page" in {
          navigator.nextPage(UkAddressPage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "UK address page -> (Non-employee-related trust) -> Start date page" in {
          navigator.nextPage(UkAddressPage, mode, baseAnswers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }


        "Non-UK address page -> (Employee-related trust) -> Company type page" in {
          navigator.nextPage(NonUkAddressPage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "Non-UK address page -> (Non-employee-related trust) -> Start date page" in {
          navigator.nextPage(NonUkAddressPage, mode, baseAnswers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Company type page -> Company time page" in {
          navigator.nextPage(CompanyTypePage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTimeController.onPageLoad(mode))
        }

        "Company time page -> Start date page" in {
          navigator.nextPage(CompanyTimePage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Start date page -> Check details page" in {
          navigator.nextPage(StartDatePage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.add.routes.CheckDetailsController.onPageLoad())
        }
      }

      "amend journey navigation" must {
        val index = 0
        val mode = CheckMode
        val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true)
          .set(IndexPage, index).success.value

        "Name page -> Do you know UTR page" in {
          navigator.nextPage(NamePage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.UtrYesNoController.onPageLoad(mode))
        }

        "Do you know UTR page -> Yes -> UTR page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, true).success.value

          navigator.nextPage(UtrYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.UtrController.onPageLoad(mode))
        }

        "UTR page -> (Employee-related trust) -> Country of Residence page" in {
          navigator.nextPage(UtrPage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "UTR page -> (Non-employee-related trust) -> Country of Residence page" in {
          navigator.nextPage(UtrPage, mode, baseAnswers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "Do you know UTR page -> No -> Country of Residence page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, false).success.value

          navigator.nextPage(UtrYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "Do you know the country of residence -> Yes -> Is residence in UK page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, true).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CountryOfResidenceInTheUkYesNoController.onPageLoad(mode))
        }

        "Do you know the country of residence -> No (Employee-related trust) -> Company type page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "Do you know the country of residence -> No (Non-employee-related trust) -> Check Details page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }


        "Is residence in UK page-> Yes -> Do you know address" in {
          val answers = baseAnswers
            .set(CountryOfResidenceInTheUkYesNoPage, true).success.value

          navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
        }

        "Is residence in UK page -> No -> Country of Residence page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceInTheUkYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CountryOfResidenceController.onPageLoad(mode))
        }

        "Country of Residence page -> No Utr -> Do you know address page" in {
          val answers = baseAnswers
            .set(CountryOfResidencePage, "ES").success.value
            .set(UtrYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidencePage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
        }

        "Country of Residence page -> With Utr (Employee-related trust) -> Company Type page" in {
          val answers = baseAnswers
            .set(CountryOfResidencePage, "ES").success.value
            .set(UtrYesNoPage, true).success.value
            .set(UtrPage, "12345678").success.value

          navigator.nextPage(CountryOfResidencePage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "Country of Residence page -> With Utr (Non-employee-related trust) -> Check details page" in {
          val answers = baseAnswers
            .set(CountryOfResidencePage, "ES").success.value
            .set(UtrYesNoPage, true).success.value
            .set(UtrPage, "12345678").success.value

          navigator.nextPage(CountryOfResidencePage, mode, answers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }

        "Do you know address page -> Yes -> Is address in UK page" in {
          val answers = baseAnswers
            .set(AddressYesNoPage, true).success.value

          navigator.nextPage(AddressYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.LiveInTheUkYesNoController.onPageLoad(mode))
        }

        "Do you know address page -> No -> Check details page" in {
          val answers = baseAnswers
            .set(AddressYesNoPage, false).success.value

          navigator.nextPage(AddressYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }

        "Is address in UK page -> Yes -> UK address page" in {
          val answers = baseAnswers
            .set(LiveInTheUkYesNoPage, true).success.value

          navigator.nextPage(LiveInTheUkYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.UkAddressController.onPageLoad(mode))
        }

        "Is address in UK page -> No -> Non-UK address page" in {
          val answers = baseAnswers
            .set(LiveInTheUkYesNoPage, false).success.value

          navigator.nextPage(LiveInTheUkYesNoPage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.NonUkAddressController.onPageLoad(mode))
        }

        "UK address page -> (Employee-related trust) -> Company type page" in {
          navigator.nextPage(UkAddressPage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "UK address page -> (Non-employee-related trust) -> Check details page" in {
          val answers = baseAnswers
            .set(IndexPage, index).success.value

          navigator.nextPage(UkAddressPage, mode, answers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }

        "Non-UK address page -> (Employee-related trust) -> Company type page" in {
          navigator.nextPage(NonUkAddressPage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTypeController.onPageLoad(mode))
        }

        "Non-UK address page -> (Non-employee-related trust) -> Check details page" in {
          val answers = baseAnswers
            .set(IndexPage, index).success.value

          navigator.nextPage(NonUkAddressPage, mode, answers, TypeOfTrust.HeritageTrust)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }

        "Company type page -> Company time page" in {
          navigator.nextPage(CompanyTypePage, mode, baseAnswers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.routes.CompanyTimeController.onPageLoad(mode))
        }

        "Company time page -> Check details page" in {
          val answers = baseAnswers
            .set(IndexPage, index).success.value

          navigator.nextPage(CompanyTimePage, mode, answers, TypeOfTrust.EmployeeRelated)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }
      }
    }
  }
}
