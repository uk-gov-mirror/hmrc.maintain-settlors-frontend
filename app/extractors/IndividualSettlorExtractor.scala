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

import com.google.inject.Inject
import models.Constant.GB
import models.settlors.IndividualSettlor
import models.{Address, CombinedPassportOrIdCard, IdCard, NationalInsuranceNumber, NonUkAddress, Passport, UkAddress, UserAnswers}
import pages.individual.living._

import scala.util.{Success, Try}

class IndividualSettlorExtractor @Inject()() {

  def apply(answers: UserAnswers, individual: IndividualSettlor, index: Int): Try[UserAnswers] =
    answers.deleteAtPath(pages.individual.living.basePath)
      .flatMap(_.set(NamePage, individual.name))
      .flatMap(answers => extractDateOfBirth(individual, answers))
      .flatMap(answers => extractCountryOfNationality(individual.countryOfNationality, answers))
      .flatMap(answers => extractCountryOfResidence(individual.countryOfResidence, answers))
      .flatMap(answers => extractAddress(individual.address, answers))
      .flatMap(answers => extractIdentification(individual, answers))
      .flatMap(_.set(MentalCapacityYesNoPage, individual.mentalCapacityYesNo))
      .flatMap(_.set(StartDatePage, individual.entityStart))
      .flatMap(_.set(IndexPage, index))

  private def extractCountryOfNationality(countryOfNationality: Option[String], answers: UserAnswers): Try[UserAnswers] = {
    if (answers.is5mldEnabled && answers.isUnderlyingData5mld) {
      countryOfNationality match {
        case Some(GB) => answers
          .set(CountryOfNationalityYesNoPage, true)
          .flatMap(_.set(CountryOfNationalityUkYesNoPage, true))
          .flatMap(_.set(CountryOfNationalityPage, GB))
        case Some(country) => answers
          .set(CountryOfNationalityYesNoPage, true)
          .flatMap(_.set(CountryOfNationalityUkYesNoPage, false))
          .flatMap(_.set(CountryOfNationalityPage, country))
        case None => answers
          .set(CountryOfNationalityYesNoPage, false)
      }
    } else {
      Success(answers)
    }
  }

  private def extractCountryOfResidence(countryOfResidence: Option[String], answers: UserAnswers): Try[UserAnswers] = {
    if (answers.is5mldEnabled && answers.isUnderlyingData5mld) {
      countryOfResidence match {
        case Some(GB) => answers
          .set(CountryOfResidenceYesNoPage, true)
          .flatMap(_.set(CountryOfResidenceUkYesNoPage, true))
          .flatMap(_.set(CountryOfResidencePage, GB))
        case Some(country) => answers
          .set(CountryOfResidenceYesNoPage, true)
          .flatMap(_.set(CountryOfResidenceUkYesNoPage, false))
          .flatMap(_.set(CountryOfResidencePage, country))
        case None => answers
          .set(CountryOfResidenceYesNoPage, false)
      }
    } else {
      Success(answers)
    }
  }

  private def extractAddress(address: Option[Address], answers: UserAnswers): Try[UserAnswers] = {
    if (answers.isTaxable) {
      address match {
        case Some(uk: UkAddress) => answers
          .set(AddressYesNoPage, true)
          .flatMap(_.set(LiveInTheUkYesNoPage, true))
          .flatMap(_.set(UkAddressPage, uk))
        case Some(nonUk: NonUkAddress) => answers
          .set(AddressYesNoPage, true)
          .flatMap(_.set(LiveInTheUkYesNoPage, false))
          .flatMap(_.set(NonUkAddressPage, nonUk))
        case _ => answers
          .set(AddressYesNoPage, false)
      }
    } else {
      Success(answers)
    }
  }

  private def extractDateOfBirth(individual: IndividualSettlor, answers: UserAnswers): Try[UserAnswers] = {
    individual.dateOfBirth match {
      case Some(dob) => answers
        .set(DateOfBirthYesNoPage, true)
        .flatMap(_.set(DateOfBirthPage, dob))
      case None => answers
        .set(DateOfBirthYesNoPage, false)
    }
  }

  private def extractIdentification(individual: IndividualSettlor,
                                    answers: UserAnswers): Try[UserAnswers] = {
    if (answers.isTaxable) {
      individual.identification match {
        case Some(NationalInsuranceNumber(nino)) => answers
          .set(NationalInsuranceNumberYesNoPage, true)
          .flatMap(_.set(NationalInsuranceNumberPage, nino))
        case Some(p: Passport) => answers
          .set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(_.set(PassportOrIdCardDetailsYesNoPage, true))
          .flatMap(_.set(PassportOrIdCardDetailsPage, p.asCombined))
        case Some(id: IdCard) => answers
          .set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(_.set(PassportOrIdCardDetailsYesNoPage, true))
          .flatMap(_.set(PassportOrIdCardDetailsPage, id.asCombined))
        case Some(combined: CombinedPassportOrIdCard) => answers
          .set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(_.set(PassportOrIdCardDetailsYesNoPage, true))
          .flatMap(_.set(PassportOrIdCardDetailsPage, combined))
        case _ => answers
          .set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(answers => extractPassportOrIdCardDetailsYesNo(individual.address, answers))
      }
    } else {
      Success(answers)
    }
  }

  private def extractPassportOrIdCardDetailsYesNo(address: Option[Address], answers: UserAnswers): Try[UserAnswers] = {
    if (address.isDefined) {
      answers.set(PassportOrIdCardDetailsYesNoPage, false)
    } else {
      Success(answers)
    }
  }

}
