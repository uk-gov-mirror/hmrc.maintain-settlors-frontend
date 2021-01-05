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

import models.settlors.BusinessSettlor
import models.{Address, CompanyType, NonUkAddress, UkAddress, UserAnswers}
import pages.business._
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsError, JsSuccess, Reads}

class BusinessSettlorMapper {

  private val logger: Logger = Logger(getClass)

  def apply(answers: UserAnswers): Option[BusinessSettlor] = {
    val readFromUserAnswers: Reads[BusinessSettlor] =
      (
        NamePage.path.read[String] and
        CompanyTypePage.path.readNullable[CompanyType] and
        CompanyTimePage.path.readNullable[Boolean] and
        UtrPage.path.readNullable[String] and
        readAddress and
        StartDatePage.path.read[LocalDate] and
        Reads(_ => JsSuccess(true))
      ) (BusinessSettlor.apply _)

    answers.data.validate[BusinessSettlor](readFromUserAnswers) match {
      case JsSuccess(value, _) =>
        Some(value)
      case JsError(errors) =>
        logger.error(s"[UTR: ${answers.utr}] Failed to rehydrate BusinessSettlor from UserAnswers due to $errors")
        None
    }
  }

  private def readAddress: Reads[Option[Address]] = {
    UtrYesNoPage.path.read[Boolean].flatMap {
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
