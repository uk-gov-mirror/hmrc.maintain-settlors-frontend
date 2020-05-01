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

package services

import java.time.LocalDate

import connectors.TrustConnector
import models.{Name, RemoveSettlor, SettlorType}
import models.settlors._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class TrustServiceSpec() extends FreeSpec with MockitoSugar with MustMatchers with ScalaFutures {

  val mockConnector: TrustConnector = mock[TrustConnector]

  val individualSettlor = IndividualSettlor(
    name = Name(firstName = "1234567890 QwErTyUiOp ,.(/)&'- name", middleName = None, lastName = "1234567890 QwErTyUiOp ,.(/)&'- name"),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    identification = None,
    address = None,
    entityStart = LocalDate.of(2012, 4, 15),
    provisional = false
  )

  val deceasedSettlor = DeceasedSettlor(
    bpMatchStatus = None,
    name = Name(firstName = "first", middleName = None, lastName = "last"),
    dateOfDeath = Some(LocalDate.parse("1993-09-24")),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    identification = None,
    address = None
  )

  val businessSettlor = BusinessSettlor(
    name = "Company Settlor Name",
    companyType = None,
    companyTime = None,
    utr = None,
    address = None,
    entityStart = LocalDate.of(2017, 2, 28),
    provisional = false
  )

  "Trust service" - {

    "get settlors" in {

      when(mockConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.successful(
          Settlors(List(individualSettlor), List(businessSettlor), Some(deceasedSettlor))
        ))

      val service = new TrustServiceImpl(mockConnector)

      implicit val hc : HeaderCarrier = HeaderCarrier()

      val result = service.getSettlors("1234567890")

      whenReady(result) {
        _ mustBe Settlors(List(individualSettlor), List(businessSettlor), Some(deceasedSettlor))
      }
    }

    "get settlor" in {

      val index = 0

      when(mockConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.successful(Settlors(List(individualSettlor), List(businessSettlor), Some(deceasedSettlor))))

      val service = new TrustServiceImpl(mockConnector)

      implicit val hc : HeaderCarrier = HeaderCarrier()

      whenReady(service.getIndividualSettlor("1234567890", index)) {
        _ mustBe individualSettlor
      }

      whenReady(service.getBusinessSettlor("1234567890", index)) {
        _ mustBe businessSettlor
      }

      whenReady(service.getDeceasedSettlor("1234567890")) {
        _ mustBe Some(deceasedSettlor)
      }
    }

    "remove settlor" in {

      when(mockConnector.removeSettlor(any(),any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, None)))

      val service = new TrustServiceImpl(mockConnector)

      val individualSettlor : RemoveSettlor =  RemoveSettlor(SettlorType.IndividualSettlor,
        index = 0,
        endDate = LocalDate.now()
      )

      val businessSettlor : RemoveSettlor =  RemoveSettlor(SettlorType.BusinessSettlor,
        index = 0,
        endDate = LocalDate.now()
      )

      implicit val hc : HeaderCarrier = HeaderCarrier()

      whenReady(service.removeSettlor("1234567890", individualSettlor)) { r =>
        r.status mustBe 200
      }

      whenReady(service.removeSettlor("1234567890", businessSettlor)) { r =>
        r.status mustBe 200
      }

    }

  }

}
