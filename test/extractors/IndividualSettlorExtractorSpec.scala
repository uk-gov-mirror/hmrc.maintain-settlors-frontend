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

package extractors

import generators.ModelGenerators
import models.Constant.GB
import models.settlors.IndividualSettlor
import models.{CombinedPassportOrIdCard, Name, NationalInsuranceNumber, UkAddress, UserAnswers}
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.individual.living._
import play.api.libs.json.Json

import java.time.LocalDate

class IndividualSettlorExtractorSpec extends FreeSpec with ScalaCheckPropertyChecks with ModelGenerators with MustMatchers {

  val answers: UserAnswers = UserAnswers(
    "Id",
    "UTRUTRUTR",
    LocalDate.of(1987, 12, 31),
    None,
    None,
    isDateOfDeathRecorded = true,
    Json.obj()
  )

  val index = 0

  val name = Name("First", None, "Last")
  val date = LocalDate.parse("1996-02-03")
  val address = UkAddress("Line 1", "Line 2", None, None, "postcode")

  val extractor = new IndividualSettlorExtractor()

  "should populate user answers when an individual has a NINO" in {

    val nino = NationalInsuranceNumber("nino")

    val individual = IndividualSettlor(
      name = name,
      dateOfBirth = Some(date),
      countryOfNationality = None,
      countryOfResidence = None,
      identification = Some(nino),
      address = None,
      mentalCapacityYesNo = None,
      entityStart = date,
      provisional = true
    )

    val result = extractor(answers, individual, Some(index)).get

    result.get(IndexPage).get mustBe index
    result.get(NamePage).get mustBe name
    result.get(DateOfBirthYesNoPage).get mustBe true
    result.get(DateOfBirthPage).get mustBe date
    result.get(CountryOfNationalityYesNoPage) mustNot be(defined)
    result.get(CountryOfNationalityUkYesNoPage) mustNot be(defined)
    result.get(CountryOfNationalityPage) mustNot be(defined)
    result.get(NationalInsuranceNumberYesNoPage).get mustBe true
    result.get(NationalInsuranceNumberPage).get mustBe "nino"
    result.get(CountryOfResidenceYesNoPage) mustNot be(defined)
    result.get(CountryOfResidenceUkYesNoPage) mustNot be(defined)
    result.get(CountryOfResidencePage) mustNot be(defined)
    result.get(AddressYesNoPage) mustNot be(defined)
    result.get(LiveInTheUkYesNoPage) mustNot be(defined)
    result.get(UkAddressPage) mustNot be(defined)
    result.get(NonUkAddressPage) mustNot be(defined)
    result.get(PassportOrIdCardDetailsYesNoPage) mustNot be(defined)
    result.get(PassportOrIdCardDetailsPage) mustNot be(defined)
    result.get(MentalCapacityYesNoPage) mustNot be(defined)
  }

  "should populate user answers when individual has a passport/ID card" in {

    val combined = CombinedPassportOrIdCard("country", "number", date)

    val individual = IndividualSettlor(
      name = name,
      dateOfBirth = Some(date),
      countryOfNationality = None,
      countryOfResidence = None,
      identification = Some(combined),
      address = Some(address),
      mentalCapacityYesNo = None,
      entityStart = date,
      provisional = true
    )

    val result = extractor(answers, individual, Some(index)).get

    result.get(IndexPage).get mustBe index
    result.get(NamePage).get mustBe name
    result.get(DateOfBirthYesNoPage).get mustBe true
    result.get(DateOfBirthPage).get mustBe date
    result.get(CountryOfNationalityYesNoPage) mustNot be(defined)
    result.get(CountryOfNationalityUkYesNoPage) mustNot be(defined)
    result.get(CountryOfNationalityPage) mustNot be(defined)
    result.get(NationalInsuranceNumberYesNoPage).get mustBe false
    result.get(NationalInsuranceNumberPage) mustNot be(defined)
    result.get(CountryOfResidenceYesNoPage) mustNot be(defined)
    result.get(CountryOfResidenceUkYesNoPage) mustNot be(defined)
    result.get(CountryOfResidencePage) mustNot be(defined)
    result.get(AddressYesNoPage).get mustBe true
    result.get(LiveInTheUkYesNoPage).get mustBe true
    result.get(UkAddressPage).get mustBe address
    result.get(NonUkAddressPage) mustNot be(defined)
    result.get(PassportOrIdCardDetailsYesNoPage).get mustBe true
    result.get(PassportOrIdCardDetailsPage).get mustBe combined
    result.get(MentalCapacityYesNoPage) mustNot be(defined)
  }

  "should populate user answers when individual has no NINO or passport/ID card" in {

    val individual = IndividualSettlor(
      name = name,
      dateOfBirth = Some(date),
      countryOfNationality = None,
      countryOfResidence = None,
      identification = None,
      address = Some(address),
      mentalCapacityYesNo = None,
      entityStart = date,
      provisional = true
    )

    val result = extractor(answers, individual, Some(index)).get

    result.get(IndexPage).get mustBe index
    result.get(NamePage).get mustBe name
    result.get(DateOfBirthYesNoPage).get mustBe true
    result.get(DateOfBirthPage).get mustBe date
    result.get(CountryOfNationalityYesNoPage) mustNot be(defined)
    result.get(CountryOfNationalityUkYesNoPage) mustNot be(defined)
    result.get(CountryOfNationalityPage) mustNot be(defined)
    result.get(NationalInsuranceNumberYesNoPage).get mustBe false
    result.get(NationalInsuranceNumberPage) mustNot be(defined)
    result.get(CountryOfResidenceYesNoPage) mustNot be(defined)
    result.get(CountryOfResidenceUkYesNoPage) mustNot be(defined)
    result.get(CountryOfResidencePage) mustNot be(defined)
    result.get(AddressYesNoPage).get mustBe true
    result.get(LiveInTheUkYesNoPage).get mustBe true
    result.get(UkAddressPage).get mustBe address
    result.get(NonUkAddressPage) mustNot be(defined)
    result.get(PassportOrIdCardDetailsYesNoPage).get mustBe false
    result.get(PassportOrIdCardDetailsPage) mustNot be(defined)
    result.get(MentalCapacityYesNoPage) mustNot be(defined)
  }

  "should populate user answers when individual has no identification or address" in {

    val individual = IndividualSettlor(
      name = name,
      dateOfBirth = None,
      countryOfNationality = None,
      countryOfResidence = None,
      identification = None,
      address = None,
      mentalCapacityYesNo = None,
      entityStart = date,
      provisional = true
    )

    val result = extractor(answers, individual, Some(index)).get

    result.get(IndexPage).get mustBe index
    result.get(NamePage).get mustBe name
    result.get(DateOfBirthYesNoPage).get mustBe false
    result.get(DateOfBirthPage) mustNot be(defined)
    result.get(CountryOfNationalityYesNoPage) mustNot be(defined)
    result.get(CountryOfNationalityUkYesNoPage) mustNot be(defined)
    result.get(CountryOfNationalityPage) mustNot be(defined)
    result.get(NationalInsuranceNumberYesNoPage).get mustBe false
    result.get(NationalInsuranceNumberPage) mustNot be(defined)
    result.get(CountryOfResidenceYesNoPage) mustNot be(defined)
    result.get(CountryOfResidenceUkYesNoPage) mustNot be(defined)
    result.get(CountryOfResidencePage) mustNot be(defined)
    result.get(AddressYesNoPage).get mustBe false
    result.get(LiveInTheUkYesNoPage) mustNot be(defined)
    result.get(UkAddressPage) mustNot be(defined)
    result.get(NonUkAddressPage) mustNot be(defined)
    result.get(PassportOrIdCardDetailsYesNoPage) mustNot be(defined)
    result.get(PassportOrIdCardDetailsPage) mustNot be(defined)
    result.get(MentalCapacityYesNoPage) mustNot be(defined)
  }

  "should populate user answers when an individual has extra 5mld data" - {

    "with UK country of Nationality and Residence" in {

      val nino = NationalInsuranceNumber("nino")

      val individual = IndividualSettlor(
        name = name,
        dateOfBirth = Some(date),
        countryOfNationality = Some(GB),
        countryOfResidence = Some(GB),
        identification = Some(nino),
        address = None,
        mentalCapacityYesNo = Some(true),
        entityStart = date,
        provisional = true
      )

      val answers5mld = answers.copy(is5mldEnabled = true, isUnderlyingData5mld = true)

      val result = extractor(answers5mld, individual, Some(index)).get

      result.get(IndexPage).get mustBe index
      result.get(NamePage).get mustBe name
      result.get(DateOfBirthYesNoPage).get mustBe true
      result.get(DateOfBirthPage).get mustBe date
      result.get(CountryOfNationalityYesNoPage).get mustBe true
      result.get(CountryOfNationalityUkYesNoPage).get mustBe true
      result.get(CountryOfNationalityPage).get mustBe GB
      result.get(NationalInsuranceNumberYesNoPage).get mustBe true
      result.get(NationalInsuranceNumberPage).get mustBe "nino"
      result.get(CountryOfResidenceYesNoPage).get mustBe true
      result.get(CountryOfResidenceUkYesNoPage).get mustBe true
      result.get(CountryOfResidencePage).get mustBe GB
      result.get(AddressYesNoPage) mustNot be(defined)
      result.get(LiveInTheUkYesNoPage) mustNot be(defined)
      result.get(UkAddressPage) mustNot be(defined)
      result.get(NonUkAddressPage) mustNot be(defined)
      result.get(PassportOrIdCardDetailsYesNoPage) mustNot be(defined)
      result.get(PassportOrIdCardDetailsPage) mustNot be(defined)
      result.get(MentalCapacityYesNoPage).get mustBe true
    }

    "with non UK country of Nationality and Residence" in {

      val nino = NationalInsuranceNumber("nino")

      val individual = IndividualSettlor(
        name = name,
        dateOfBirth = Some(date),
        countryOfNationality = Some("FR"),
        countryOfResidence = Some("FR"),
        identification = Some(nino),
        address = None,
        mentalCapacityYesNo = Some(false),
        entityStart = date,
        provisional = true
      )

      val answers5mld = answers.copy(is5mldEnabled = true, isUnderlyingData5mld = true)

      val result = extractor(answers5mld, individual, Some(index)).get

      result.get(IndexPage).get mustBe index
      result.get(NamePage).get mustBe name
      result.get(DateOfBirthYesNoPage).get mustBe true
      result.get(DateOfBirthPage).get mustBe date
      result.get(CountryOfNationalityYesNoPage).get mustBe true
      result.get(CountryOfNationalityUkYesNoPage).get mustBe false
      result.get(CountryOfNationalityPage).get mustBe "FR"
      result.get(NationalInsuranceNumberYesNoPage).get mustBe true
      result.get(NationalInsuranceNumberPage).get mustBe "nino"
      result.get(CountryOfResidenceYesNoPage).get mustBe true
      result.get(CountryOfResidenceUkYesNoPage).get mustBe false
      result.get(CountryOfResidencePage).get mustBe "FR"
      result.get(AddressYesNoPage) mustNot be(defined)
      result.get(LiveInTheUkYesNoPage) mustNot be(defined)
      result.get(UkAddressPage) mustNot be(defined)
      result.get(NonUkAddressPage) mustNot be(defined)
      result.get(PassportOrIdCardDetailsYesNoPage) mustNot be(defined)
      result.get(PassportOrIdCardDetailsPage) mustNot be(defined)
      result.get(MentalCapacityYesNoPage).get mustBe false
    }

    "with unknown country of Nationality and Residence" in {

      val nino = NationalInsuranceNumber("nino")

      val individual = IndividualSettlor(
        name = name,
        dateOfBirth = Some(date),
        countryOfNationality = None,
        countryOfResidence = None,
        identification = Some(nino),
        address = None,
        mentalCapacityYesNo = Some(true),
        entityStart = date,
        provisional = true
      )

      val answers5mld = answers.copy(is5mldEnabled = true, isUnderlyingData5mld = true)

      val result = extractor(answers5mld, individual, Some(index)).get

      result.get(IndexPage).get mustBe index
      result.get(NamePage).get mustBe name
      result.get(DateOfBirthYesNoPage).get mustBe true
      result.get(DateOfBirthPage).get mustBe date
      result.get(CountryOfNationalityYesNoPage).get mustBe false
      result.get(CountryOfNationalityUkYesNoPage) mustNot be(defined)
      result.get(CountryOfNationalityPage) mustNot be(defined)
      result.get(NationalInsuranceNumberYesNoPage).get mustBe true
      result.get(NationalInsuranceNumberPage).get mustBe "nino"
      result.get(CountryOfResidenceYesNoPage).get mustBe false
      result.get(CountryOfResidenceUkYesNoPage) mustNot be(defined)
      result.get(CountryOfResidencePage) mustNot be(defined)
      result.get(AddressYesNoPage) mustNot be(defined)
      result.get(LiveInTheUkYesNoPage) mustNot be(defined)
      result.get(UkAddressPage) mustNot be(defined)
      result.get(NonUkAddressPage) mustNot be(defined)
      result.get(PassportOrIdCardDetailsYesNoPage) mustNot be(defined)
      result.get(PassportOrIdCardDetailsPage) mustNot be(defined)
      result.get(MentalCapacityYesNoPage).get mustBe true
    }
  }

  "should populate user answers when a non taxable trust" - {

    "with UK country of Nationality and Residence" in {

      val individual = IndividualSettlor(
        name = name,
        dateOfBirth = Some(date),
        countryOfNationality = Some(GB),
        countryOfResidence = Some(GB),
        identification = None,
        address = None,
        mentalCapacityYesNo = Some(true),
        entityStart = date,
        provisional = true
      )

      val answers5mld = answers.copy(is5mldEnabled = true, isUnderlyingData5mld = true, isTaxable = false)

      val result = extractor(answers5mld, individual, Some(index)).get

      result.get(IndexPage).get mustBe index
      result.get(NamePage).get mustBe name
      result.get(DateOfBirthYesNoPage).get mustBe true
      result.get(DateOfBirthPage).get mustBe date
      result.get(CountryOfNationalityYesNoPage).get mustBe true
      result.get(CountryOfNationalityUkYesNoPage).get mustBe true
      result.get(CountryOfNationalityPage).get mustBe GB
      result.get(NationalInsuranceNumberYesNoPage) mustNot be(defined)
      result.get(NationalInsuranceNumberPage) mustNot be(defined)
      result.get(CountryOfResidenceYesNoPage).get mustBe true
      result.get(CountryOfResidenceUkYesNoPage).get mustBe true
      result.get(CountryOfResidencePage).get mustBe GB
      result.get(AddressYesNoPage) mustNot be(defined)
      result.get(LiveInTheUkYesNoPage) mustNot be(defined)
      result.get(UkAddressPage) mustNot be(defined)
      result.get(NonUkAddressPage) mustNot be(defined)
      result.get(PassportOrIdCardDetailsYesNoPage) mustNot be(defined)
      result.get(PassportOrIdCardDetailsPage) mustNot be(defined)
      result.get(MentalCapacityYesNoPage).get mustBe true
    }

    "with non UK country of Nationality and Residence" in {

      val individual = IndividualSettlor(
        name = name,
        dateOfBirth = Some(date),
        countryOfNationality = Some("FR"),
        countryOfResidence = Some("FR"),
        identification = None,
        address = None,
        mentalCapacityYesNo = Some(false),
        entityStart = date,
        provisional = true
      )

      val answers5mld = answers.copy(is5mldEnabled = true, isUnderlyingData5mld = true, isTaxable = false)

      val result = extractor(answers5mld, individual, Some(index)).get

      result.get(IndexPage).get mustBe index
      result.get(NamePage).get mustBe name
      result.get(DateOfBirthYesNoPage).get mustBe true
      result.get(DateOfBirthPage).get mustBe date
      result.get(CountryOfNationalityYesNoPage).get mustBe true
      result.get(CountryOfNationalityUkYesNoPage).get mustBe false
      result.get(CountryOfNationalityPage).get mustBe "FR"
      result.get(NationalInsuranceNumberYesNoPage) mustNot be(defined)
      result.get(NationalInsuranceNumberPage) mustNot be(defined)
      result.get(CountryOfResidenceYesNoPage).get mustBe true
      result.get(CountryOfResidenceUkYesNoPage).get mustBe false
      result.get(CountryOfResidencePage).get mustBe "FR"
      result.get(AddressYesNoPage) mustNot be(defined)
      result.get(LiveInTheUkYesNoPage) mustNot be(defined)
      result.get(UkAddressPage) mustNot be(defined)
      result.get(NonUkAddressPage) mustNot be(defined)
      result.get(PassportOrIdCardDetailsYesNoPage) mustNot be(defined)
      result.get(PassportOrIdCardDetailsPage) mustNot be(defined)
      result.get(MentalCapacityYesNoPage).get mustBe false
    }

    "with unknown country of Nationality and Residence" in {

      val individual = IndividualSettlor(
        name = name,
        dateOfBirth = Some(date),
        countryOfNationality = None,
        countryOfResidence = None,
        identification = None,
        address = None,
        mentalCapacityYesNo = Some(true),
        entityStart = date,
        provisional = true
      )

      val answers5mld = answers.copy(is5mldEnabled = true, isUnderlyingData5mld = true, isTaxable = false)

      val result = extractor(answers5mld, individual, Some(index)).get

      result.get(IndexPage).get mustBe index
      result.get(NamePage).get mustBe name
      result.get(DateOfBirthYesNoPage).get mustBe true
      result.get(DateOfBirthPage).get mustBe date
      result.get(CountryOfNationalityYesNoPage).get mustBe false
      result.get(CountryOfNationalityUkYesNoPage) mustNot be(defined)
      result.get(CountryOfNationalityPage) mustNot be(defined)
      result.get(NationalInsuranceNumberYesNoPage) mustNot be(defined)
      result.get(NationalInsuranceNumberPage) mustNot be(defined)
      result.get(CountryOfResidenceYesNoPage).get mustBe false
      result.get(CountryOfResidenceUkYesNoPage) mustNot be(defined)
      result.get(CountryOfResidencePage) mustNot be(defined)
      result.get(AddressYesNoPage) mustNot be(defined)
      result.get(LiveInTheUkYesNoPage) mustNot be(defined)
      result.get(UkAddressPage) mustNot be(defined)
      result.get(NonUkAddressPage) mustNot be(defined)
      result.get(PassportOrIdCardDetailsYesNoPage) mustNot be(defined)
      result.get(PassportOrIdCardDetailsPage) mustNot be(defined)
      result.get(MentalCapacityYesNoPage).get mustBe true
    }
  }
}
