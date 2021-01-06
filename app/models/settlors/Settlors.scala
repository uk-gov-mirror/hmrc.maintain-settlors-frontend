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

package models.settlors

import play.api.i18n.{Messages, MessagesProvider}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, __}

trait Settlor

case class Settlors(settlor: List[IndividualSettlor],
                    settlorCompany: List[BusinessSettlor],
                    deceased: Option[DeceasedSettlor]) {

  val size: Int = (settlor ++ settlorCompany ++ deceased).size

  val hasLivingSettlors: Boolean = settlor.nonEmpty || settlorCompany.nonEmpty

  def addToHeading()(implicit mp: MessagesProvider): String = {

    size match {
      case 0 => Messages("addASettlor.heading")
      case 1 => Messages("addASettlor.singular.heading")
      case l => Messages("addASettlor.count.heading", l)
    }
  }

  val isMaxedOut: Boolean = {
    (settlor ++ settlorCompany).size >= 25
  }

}

object Settlors {
  implicit val reads: Reads[Settlors] =
    ((__ \ "settlors" \ "settlor").readWithDefault[List[IndividualSettlor]](Nil)
      and (__ \ "settlors" \ "settlorCompany").readWithDefault[List[BusinessSettlor]](Nil)
      and (__ \ "settlors" \ "deceased").readNullable[DeceasedSettlor]
      ).apply(Settlors.apply _)
}