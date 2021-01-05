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

import java.time.LocalDate

import generators.ModelGenerators
import models.settlors.BusinessSettlor
import models.{TypeOfTrust, UkAddress, UserAnswers}
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.business._
import play.api.libs.json.Json

class BusinessSettlorExtractorSpec extends FreeSpec with ScalaCheckPropertyChecks with ModelGenerators with MustMatchers {

  private val answers: UserAnswers = UserAnswers(
    "Id",
    "UTRUTRUTR",
    LocalDate.of(1987, 12, 31),
    TypeOfTrust.WillTrustOrIntestacyTrust,
    None,
    isDateOfDeathRecorded = true,
    Json.obj()
  )

  private val index = 0

  private val name = "Name"
  private val utr = "1234567890"
  private val date = LocalDate.parse("1996-02-03")
  private val address = UkAddress("Line 1", "Line 2", None, None, "postcode")

  private val extractor = new BusinessSettlorExtractor()

  "should populate user answers when the business has a UTR" in {

    val business = BusinessSettlor(
      name = name,
      companyType = None,
      companyTime = None,
      utr = Some(utr),
      address = None,
      entityStart = date,
      provisional = true
    )

    val result = extractor(answers, business, index).get

    result.get(IndexPage).get mustBe index
    result.get(NamePage).get mustBe name
    result.get(UtrYesNoPage).get mustBe true
    result.get(UtrPage).get mustBe utr
    result.get(AddressYesNoPage) mustNot be(defined)
    result.get(LiveInTheUkYesNoPage) mustNot be(defined)
    result.get(UkAddressPage) mustNot be(defined)
    result.get(NonUkAddressPage) mustNot be(defined)
    result.get(StartDatePage).get mustBe date
  }


  "should populate user answers when the business has an address" in {

    val business = BusinessSettlor(
      name = name,
      companyType = None,
      companyTime = None,
      utr = None,
      address = Some(address),
      entityStart = date,
      provisional = true
    )

    val result = extractor(answers, business, index).get

    result.get(IndexPage).get mustBe index
    result.get(NamePage).get mustBe name
    result.get(UtrYesNoPage).get mustBe false
    result.get(UtrPage) mustNot be(defined)
    result.get(AddressYesNoPage).get mustBe true
    result.get(LiveInTheUkYesNoPage).get mustBe true
    result.get(UkAddressPage).get mustBe address
    result.get(NonUkAddressPage) mustNot be(defined)
    result.get(StartDatePage).get mustBe date

  }

  "should populate user answers when the business has no UTR or address" in {

    val business = BusinessSettlor(
      name = name,
      companyType = None,
      companyTime = None,
      utr = None,
      address = None,
      entityStart = date,
      provisional = true
    )

    val result = extractor(answers, business, index).get

    result.get(IndexPage).get mustBe index
    result.get(NamePage).get mustBe name
    result.get(UtrYesNoPage).get mustBe false
    result.get(UtrPage) mustNot be(defined)
    result.get(AddressYesNoPage).get mustBe false
    result.get(LiveInTheUkYesNoPage) mustNot be(defined)
    result.get(UkAddressPage) mustNot be(defined)
    result.get(NonUkAddressPage) mustNot be(defined)
    result.get(StartDatePage).get mustBe date
  }

}
