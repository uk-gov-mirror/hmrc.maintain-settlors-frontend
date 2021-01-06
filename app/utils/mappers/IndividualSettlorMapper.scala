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

package utils.mappers

import java.time.LocalDate

import models.settlors.IndividualSettlor
import models.{Address, CombinedPassportOrIdCard, IdCard, IndividualIdentification, Name, NationalInsuranceNumber, NonUkAddress, Passport, UkAddress, UserAnswers}
import pages.individual.living._
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Reads}
import play.api.libs.functional.syntax._

class IndividualSettlorMapper {

  private val logger: Logger = Logger(getClass)

  def apply(answers: UserAnswers): Option[IndividualSettlor] = {
    val readFromUserAnswers: Reads[IndividualSettlor] =
      (
        NamePage.path.read[Name] and
        DateOfBirthPage.path.readNullable[LocalDate] and
        readIdentification and
        readAddress and
        StartDatePage.path.read[LocalDate] and
        Reads(_ => JsSuccess(true))
      ) (IndividualSettlor.apply _)

    answers.data.validate[IndividualSettlor](readFromUserAnswers) match {
      case JsSuccess(value, _) =>
        Some(value)
      case JsError(errors) =>
        logger.error(s"[UTR: ${answers.utr}] Failed to rehydrate IndividualSettlor from UserAnswers due to $errors")
        None
    }
  }

  private def readIdentification: Reads[Option[IndividualIdentification]] = {
    NationalInsuranceNumberYesNoPage.path.read[Boolean].flatMap[Option[IndividualIdentification]] {
      case true => NationalInsuranceNumberPage.path.read[String].map(nino => Some(NationalInsuranceNumber(nino)))
      case false => readPassportOrIdCard
    }
  }

  private def readPassportOrIdCard: Reads[Option[IndividualIdentification]] = {
    (for {
      hasNino <- NationalInsuranceNumberYesNoPage.path.readWithDefault(false)
      hasAddress <- AddressYesNoPage.path.readWithDefault(false)
      hasPassport <- PassportDetailsYesNoPage.path.readWithDefault(false)
      hasIdCard <- IdCardDetailsYesNoPage.path.readWithDefault(false)
      hasPassportOrIdCard <- PassportOrIdCardDetailsYesNoPage.path.readWithDefault(false)
    } yield (hasNino, hasAddress, hasPassport, hasIdCard, hasPassportOrIdCard)).flatMap[Option[IndividualIdentification]] {
        case (false, true, true, false, _) => PassportDetailsPage.path.read[Passport].map(Some(_))
        case (false, true, false, true, _) => IdCardDetailsPage.path.read[IdCard].map(Some(_))
        case (false, true, false, false, true) => PassportOrIdCardDetailsPage.path.read[CombinedPassportOrIdCard].map(Some(_))
        case _ => Reads(_ => JsSuccess(None))
      }
  }

  private def readAddress: Reads[Option[Address]] = {
    NationalInsuranceNumberYesNoPage.path.read[Boolean].flatMap {
      case true => Reads(_ => JsSuccess(None))
      case false => AddressYesNoPage.path.read[Boolean].flatMap[Option[Address]] {
        case true => readUkOrNonUkAddress
        case false => Reads(_ => JsSuccess(None))
      }
    }
  }

  private def readUkOrNonUkAddress: Reads[Option[Address]] = {
    LiveInTheUkYesNoPage.path.read[Boolean].flatMap[Option[Address]] {
      case true => UkAddressPage.path.read[UkAddress].map(Some(_))
      case false => NonUkAddressPage.path.read[NonUkAddress].map(Some(_))
    }
  }

}
