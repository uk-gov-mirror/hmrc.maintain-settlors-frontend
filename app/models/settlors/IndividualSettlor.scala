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

import java.time.LocalDate

import models.{Address, IndividualIdentification, Name}
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class IndividualSettlor(name: Name,
                                   dateOfBirth: Option[LocalDate],
                                   identification: Option[IndividualIdentification],
                                   address : Option[Address],
                                   entityStart: LocalDate,
                                   provisional : Boolean) extends Settlor

object IndividualSettlor {

  implicit val reads: Reads[IndividualSettlor] =
    ((__ \ 'name).read[Name] and
      (__ \ 'dateOfBirth).readNullable[LocalDate] and
      __.lazyRead(readNullableAtSubPath[IndividualIdentification](__ \ 'identification)) and
      __.lazyRead(readNullableAtSubPath[Address](__ \ 'identification \ 'address)) and
      (__ \ "entityStart").read[LocalDate] and
      (__ \ "provisional").readWithDefault(false)).tupled.map{

      case (name, dob, nino, identification, entityStart, provisional) =>
        IndividualSettlor(name, dob, nino, identification, entityStart, provisional)

    }

  implicit val writes: Writes[IndividualSettlor] =
    ((__ \ 'name).write[Name] and
      (__ \ 'dateOfBirth).writeNullable[LocalDate] and
      (__ \ 'identification).writeNullable[IndividualIdentification] and
      (__ \ 'identification \ 'address).writeNullable[Address] and
      (__ \ "entityStart").write[LocalDate] and
      (__ \ "provisional").write[Boolean]
      ).apply(unlift(IndividualSettlor.unapply))

  def readNullableAtSubPath[T:Reads](subPath : JsPath) : Reads[Option[T]] = Reads (
    _.transform(subPath.json.pick)
      .flatMap(_.validate[T])
      .map(Some(_))
      .recoverWith(_ => JsSuccess(None))
  )
}