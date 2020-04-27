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

package models.settlors

import models.settlors.TypeOfSettlorToAdd._
import play.api.i18n.{Messages, MessagesProvider}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, __}
import viewmodels.RadioOption

trait Settlor

case class Settlors(settlor: List[IndividualSettlor],
                    settlorCompany: List[BusinessSettlor]) {

  type SettlorOption = (Int, TypeOfSettlorToAdd)
  type SettlorOptions = List[SettlorOption]

  def addToHeading()(implicit mp: MessagesProvider): String =
    (settlor ++ settlorCompany).size match {
      case 0 => Messages("addASettlor.heading")
      case 1 => Messages("addASettlor.singular.heading")
      case l => Messages("addASettlor.count.heading", l)
    }

  private val options: SettlorOptions = {
    (settlor.size, Individual) ::
    (settlorCompany.size, Business) ::
    Nil
  }

  val nonMaxedOutOptions: List[RadioOption] = {
    options.filter(x => x._1 < 25).map {
      x => RadioOption(TypeOfSettlorToAdd.prefix, x._2.toString)
    }
  }

  val maxedOutOptions: List[RadioOption] = {
    options.filter(x => x._1 >= 25).map {
      x => RadioOption(TypeOfSettlorToAdd.prefix, x._2.toString)
    }
  }

}

object Settlors {
  implicit val reads: Reads[Settlors] =
    ((__ \ "settlors" \ "settlor").readWithDefault[List[IndividualSettlor]](Nil)
      and (__ \ "settlors" \ "settlorCompany").readWithDefault[List[BusinessSettlor]](Nil)
      ).apply(Settlors.apply _)
}