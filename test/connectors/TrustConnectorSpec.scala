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

package connectors

import java.time.LocalDate

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import generators.Generators
import models.DeedOfVariation.PreviouslyAbsoluteInterestUnderWill
import models.settlors.{BusinessSettlor, DeceasedSettlor, IndividualSettlor, Settlors}
import models.{CompanyType, Name, TrustDetails, TypeOfTrust}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Inside}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

class TrustConnectorSpec extends SpecBase with Generators with ScalaFutures
  with Inside with BeforeAndAfterAll with BeforeAndAfterEach with IntegrationPatience {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  protected val server: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  val utr = "1000000008"
  val index = 0
  val description = "description"
  val date: LocalDate = LocalDate.parse("2019-02-03")

  "trust connector" when {

    "get trusts details" in {

      val utr = "1000000008"

      val json = Json.parse(
        """
          |{
          | "startDate": "1920-03-28",
          | "lawCountry": "AD",
          | "administrationCountry": "GB",
          | "residentialStatus": {
          |   "uk": {
          |     "scottishLaw": false,
          |     "preOffShore": "AD"
          |   }
          | },
          | "typeOfTrust": "Will Trust or Intestacy Trust",
          | "deedOfVariation": "Previously there was only an absolute interest under the will",
          | "interVivos": false
          |}
          |""".stripMargin)

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        get(urlEqualTo(s"/trusts/$utr/trust-details"))
          .willReturn(okJson(json.toString))
      )

      val processed = connector.getTrustDetails(utr)

      whenReady(processed) {
        r =>
          r mustBe TrustDetails(startDate = LocalDate.parse("1920-03-28"), typeOfTrust = TypeOfTrust.WillTrustOrIntestacyTrust, Some(PreviouslyAbsoluteInterestUnderWill))
      }

    }

    "get settlors returns a trust with empty lists" must {

      "return a default empty list beneficiaries" in {

        val utr = "1000000008"

        val json = Json.parse(
          """
            |{
            | "settlors": {
            | }
            |}
            |""".stripMargin)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/$utr/transformed/settlors"))
            .willReturn(okJson(json.toString))
        )

        val processed = connector.getSettlors(utr)

        whenReady(processed) {
          result =>
            result mustBe Settlors(settlor = Nil, settlorCompany = Nil, None)
        }

        application.stop()
      }

    }

    "get settlors" must {

      "parse the response and return the settlors" in {
        val utr = "1000000008"

        val json = Json.parse(
          """
            |{
            | "settlors" : {
            |   "settlor" : [
            |     {
            |       "lineNo" : "79",
            |       "name" : {
            |         "firstName" : "Carmel",
            |         "lastName" : "Settlor"
            |       },
            |       "entityStart" : "2019-09-23"
            |     }
            |   ],
            |   "settlorCompany" : [
            |     {
            |       "lineNo" : "110",
            |       "bpMatchStatus" : "98",
            |       "name" : "Settlor Org 24",
            |       "companyType" : "Investment",
            |       "companyTime" : false,
            |       "entityStart" : "2019-09-23"
            |     }
            |   ],
            |   "deceased" : {
            |     "name" : {
            |       "firstName" : "Carmel",
            |       "lastName" : "Settlor"
            |     }
            |   }
            | }
            |}
            |""".stripMargin)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/$utr/transformed/settlors"))
            .willReturn(okJson(json.toString))
        )

        val processed = connector.getSettlors(utr)

        whenReady(processed) {
          result =>
            result mustBe Settlors(settlor = List(
              IndividualSettlor(
                name = Name("Carmel", None, "Settlor"),
                dateOfBirth = None,
                identification = None,
                address = None,
                entityStart = LocalDate.parse("2019-09-23"),
                provisional = false
              )
            ), settlorCompany = List(
              BusinessSettlor(
                name = "Settlor Org 24",
                companyType = Some(CompanyType.Investment),
                companyTime = Some(false),
                utr = None,
                address = None,
                entityStart = LocalDate.parse("2019-09-23"),
                provisional = false
              )
            ), deceased = Some(
              DeceasedSettlor(None, Name("Carmel", None, "Settlor"), None, None, None, None))
            )
        }

        application.stop()
      }

    }

    "amending an individual settlor" must {

      def amendIndividualSettlorUrl(utr: String, index: Int) =
        s"/trusts/amend-individual-settlor/$utr/$index"

      "Return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendIndividualSettlorUrl(utr, index)))
            .willReturn(ok)
        )

        val individual = IndividualSettlor(
          name = Name(
            firstName = "First",
            middleName = None,
            lastName = "Last"
          ),
          dateOfBirth = None,
          identification = None,
          address = None,
          entityStart = LocalDate.parse("2020-03-27"),
          provisional = false
        )

        val result = connector.amendIndividualSettlor(utr, index, individual)

        result.futureValue.status mustBe OK

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendIndividualSettlorUrl(utr, index)))
            .willReturn(badRequest)
        )

        val individual = IndividualSettlor(
          name = Name(
            firstName = "First",
            middleName = None,
            lastName = "Last"
          ),
          dateOfBirth = None,
          identification = None,
          address = None,
          entityStart = LocalDate.parse("2020-03-27"),
          provisional = false
        )

        val result = connector.amendIndividualSettlor(utr, index, individual)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "amending a business settlor" must {

      def amendBusinessSettlorUrl(utr: String, index: Int) =
        s"/trusts/amend-business-settlor/$utr/$index"

      val business = BusinessSettlor(
        name = "Name",
        companyType = None,
        companyTime = None,
        utr = None,
        address = None,
        entityStart = LocalDate.parse("2020-03-27"),
        provisional = false
      )

      "Return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendBusinessSettlorUrl(utr, index)))
            .willReturn(ok)
        )

        val result = connector.amendBusinessSettlor(utr, index, business)

        result.futureValue.status mustBe OK

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendBusinessSettlorUrl(utr, index)))
            .willReturn(badRequest)
        )

        val result = connector.amendBusinessSettlor(utr, index, business)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

  }

  "amending a deceasd settlor" must {

    def amendDeceasedSettlorUrl(utr: String) =
      s"/trusts/amend-deceased-settlor/$utr"

    "Return OK when the request is successful" in {

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        post(urlEqualTo(amendDeceasedSettlorUrl(utr)))
          .willReturn(ok)
      )

      val individual = DeceasedSettlor(
        bpMatchStatus = None,
        name = Name(
          firstName = "First",
          middleName = None,
          lastName = "Last"
        ),
        dateOfDeath = None,
        dateOfBirth = None,
        identification = None,
        address = None
      )

      val result = connector.amendDeceasedSettlor(utr, individual)

      result.futureValue.status mustBe OK

      application.stop()
    }

    "return Bad Request when the request is unsuccessful" in {

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        post(urlEqualTo(amendDeceasedSettlorUrl(utr)))
          .willReturn(badRequest)
      )

      val individual = DeceasedSettlor(
        bpMatchStatus = None,
        name = Name(
          firstName = "First",
          middleName = None,
          lastName = "Last"
        ),
        dateOfDeath = None,
        dateOfBirth = None,
        identification = None,
        address = None
      )

      val result = connector.amendDeceasedSettlor(utr, individual)

      result.map(response => response.status mustBe BAD_REQUEST)

      application.stop()
    }
  }
}
