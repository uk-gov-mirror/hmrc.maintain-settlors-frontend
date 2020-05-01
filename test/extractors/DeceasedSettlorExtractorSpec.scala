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

package extractors

import java.time.LocalDate

import generators.ModelGenerators
import models.settlors.DeceasedSettlor
import models.{CombinedPassportOrIdCard, Name, NationalInsuranceNumber, TypeOfTrust, UkAddress, UserAnswers}
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.individual.deceased._
import play.api.libs.json.Json

class DeceasedSettlorExtractorSpec extends FreeSpec with ScalaCheckPropertyChecks with ModelGenerators with MustMatchers {

  val answers: UserAnswers = UserAnswers(
    "Id",
    "UTRUTRUTR",
    LocalDate.of(1987, 12, 31),
    TypeOfTrust.WillTrustOrIntestacyTrust,
    None,
    isDateOfDeathRecorded = true,
    Json.obj()
  )

  val name = Name("First", None, "Last")
  val date = LocalDate.parse("1967-02-03")
  val dateOfDeath = LocalDate.parse("1957-02-03")
  val address = UkAddress("Line 1", "Line 2", None, None, "postcode")

  val extractor = new DeceasedSettlorExtractor()

  "should populate user answers when an individual has a NINO" in {

    val nino = NationalInsuranceNumber("nino")

    val individual = DeceasedSettlor(
      bpMatchStatus = None,
      name = name,
      dateOfDeath = Some(dateOfDeath),
      dateOfBirth = Some(date),
      identification = Some(nino),
      address = None
    )

    val result = extractor(answers, individual).get

    result.get(NamePage).get mustBe name
    result.get(DateOfDeathYesNoPage).get mustBe true
    result.get(DateOfDeathPage).get mustBe dateOfDeath
    result.get(DateOfBirthYesNoPage).get mustBe true
    result.get(DateOfBirthPage).get mustBe date
    result.get(NationalInsuranceNumberYesNoPage).get mustBe true
    result.get(NationalInsuranceNumberPage).get mustBe "nino"
    result.get(AddressYesNoPage) mustNot be(defined)
    result.get(LivedInTheUkYesNoPage) mustNot be(defined)
    result.get(UkAddressPage) mustNot be(defined)
    result.get(NonUkAddressPage) mustNot be(defined)
  }

  "should populate user answers when an individual has no Nino but an Address" in {

    val nino = NationalInsuranceNumber("nino")

    val individual = DeceasedSettlor(
      bpMatchStatus = None,
      name = name,
      dateOfDeath = Some(dateOfDeath),
      dateOfBirth = Some(date),
      identification = None,
      address = Some(address)
    )

    val result = extractor(answers, individual).get

    result.get(NamePage).get mustBe name
    result.get(DateOfDeathYesNoPage).get mustBe true
    result.get(DateOfDeathPage).get mustBe dateOfDeath
    result.get(DateOfBirthYesNoPage).get mustBe true
    result.get(DateOfBirthPage).get mustBe date
    result.get(NationalInsuranceNumberYesNoPage).get mustBe false
    result.get(NationalInsuranceNumberPage) mustNot be(defined)
    result.get(AddressYesNoPage).get mustBe true
    result.get(LivedInTheUkYesNoPage).get mustBe true
    result.get(UkAddressPage).get mustBe address
    result.get(NonUkAddressPage) mustNot be(defined)
  }

  "should populate user answers when individual has only a name" in {

    val individual = DeceasedSettlor(
      bpMatchStatus = None,
      name = name,
      dateOfDeath = None,
      dateOfBirth = None,
      identification = None,
      address = None
    )

    val result = extractor(answers, individual).get

    result.get(NamePage).get mustBe name
    result.get(DateOfBirthYesNoPage).get mustBe false
    result.get(DateOfBirthPage) mustNot be(defined)
    result.get(NationalInsuranceNumberYesNoPage).get mustBe false
    result.get(NationalInsuranceNumberPage) mustNot be(defined)
    result.get(AddressYesNoPage).get mustBe false
    result.get(LivedInTheUkYesNoPage) mustNot be(defined)
    result.get(UkAddressPage) mustNot be(defined)
    result.get(NonUkAddressPage) mustNot be(defined)
  }

}
