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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import generators.Generators
import models.DeedOfVariation.PreviouslyAbsoluteInterestUnderWill
import models.settlors.{BusinessSettlor, DeceasedSettlor, IndividualSettlor, Settlors}
import models.{CompanyType, Name, RemoveSettlor, SettlorType, TrustDetails, TypeOfTrust}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Inside}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.{JsBoolean, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

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

  private val trustsUrl: String = "/trusts"
  private val settlorsUrl: String = s"$trustsUrl/settlors"

  private def getTrustDetailsUrl(utr: String) = s"$trustsUrl/$utr/trust-details"
  private def getSettlorsUrl(utr: String) = s"$settlorsUrl/$utr/transformed"
  private def getIsDeceasedSettlorDateOfDeathRecordedUrl(utr: String) = s"$settlorsUrl/$utr/transformed/deceased-settlor-death-recorded"
  private def addIndividualSettlorUrl(utr: String) = s"$settlorsUrl/add-individual/$utr"
  private def amendIndividualSettlorUrl(utr: String, index: Int) = s"$settlorsUrl/amend-individual/$utr/$index"
  private def addBusinessSettlorUrl(utr: String) = s"$settlorsUrl/add-business/$utr"
  private def amendBusinessSettlorUrl(utr: String, index: Int) = s"$settlorsUrl/amend-business/$utr/$index"
  private def amendDeceasedSettlorUrl(utr: String) = s"$settlorsUrl/amend-deceased/$utr"
  private def removeSettlorUrl(utr: String) = s"$settlorsUrl/$utr/remove"

  private val individual = IndividualSettlor(
    name = Name("Carmel", None, "Settlor"),
    dateOfBirth = None,
    identification = None,
    address = None,
    entityStart = date,
    provisional = false
  )

  private val business = BusinessSettlor(
    name = "Settlor Org 24",
    companyType = Some(CompanyType.Investment),
    companyTime = Some(false),
    utr = None,
    address = None,
    entityStart = date,
    provisional = false
  )

  private val deceased = DeceasedSettlor(
    bpMatchStatus = None,
    name = Name("Carmel", None, "Settlor"),
    dateOfBirth = None,
    dateOfDeath = None,
    identification = None,
    address = None
  )

  "trust connector" when {

    "getTrustsDetails" in {

      val json = Json.parse(
        """
          |{
          | "startDate": "2019-02-03",
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
        get(urlEqualTo(getTrustDetailsUrl(utr)))
          .willReturn(okJson(json.toString))
      )

      val processed = connector.getTrustDetails(utr)

      whenReady(processed) {
        r =>
          r mustBe TrustDetails(startDate = date, typeOfTrust = TypeOfTrust.WillTrustOrIntestacyTrust, Some(PreviouslyAbsoluteInterestUnderWill), None)
      }

    }

    "getSettlors" when {

      "there are no settlors" must {

        "return a default empty list of settlors" in {

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
            get(urlEqualTo(getSettlorsUrl(utr)))
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

      "there are settlors" must {

        "parse the response and return the settlors" in {

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
              |       "entityStart" : "2019-02-03"
              |     }
              |   ],
              |   "settlorCompany" : [
              |     {
              |       "lineNo" : "110",
              |       "bpMatchStatus" : "98",
              |       "name" : "Settlor Org 24",
              |       "companyType" : "Investment",
              |       "companyTime" : false,
              |       "entityStart" : "2019-02-03"
              |     }
              |   ],
              |    "deceased" : {
              |       "name" : {
              |         "firstName" : "Carmel",
              |         "lastName" : "Settlor"
              |       }
              |     }
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
            get(urlEqualTo(getSettlorsUrl(utr)))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.getSettlors(utr)

          whenReady(processed) {
            result =>
              result mustBe Settlors(
                settlor = List(individual),
                settlorCompany = List(business),
                deceased = Some(deceased)
              )
          }

          application.stop()
        }
      }
    }

    "addIndividualSettlor" must {

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
          post(urlEqualTo(addIndividualSettlorUrl(utr)))
            .willReturn(ok)
        )

        val result = connector.addIndividualSettlor(utr, individual)

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
          post(urlEqualTo(addIndividualSettlorUrl(utr)))
            .willReturn(badRequest)
        )

        val result = connector.addIndividualSettlor(utr, individual)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "amendIndividualSettlor" must {

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

        val result = connector.amendIndividualSettlor(utr, index, individual)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "addBusinessSettlor" must {

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
          post(urlEqualTo(addBusinessSettlorUrl(utr)))
            .willReturn(ok)
        )

        val result = connector.addBusinessSettlor(utr, business)

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
          post(urlEqualTo(addBusinessSettlorUrl(utr)))
            .willReturn(badRequest)
        )

        val result = connector.addBusinessSettlor(utr, business)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "amendBusinessSettlor" must {

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

    "amendDeceasedSettlor" must {

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

        val result = connector.amendDeceasedSettlor(utr, deceased)

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

        val result = connector.amendDeceasedSettlor(utr, deceased)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }
    }

    "getIsDeceasedSettlorDateOfDeathRecorded" must {

      "Return true or false when the request is successful" in {

        val json = JsBoolean(true)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(getIsDeceasedSettlorDateOfDeathRecordedUrl(utr)))
            .willReturn(okJson(json.toString))
        )

        val processed = connector.getIsDeceasedSettlorDateOfDeathRecorded(utr)

        whenReady(processed) {
          result =>
            result.value mustBe true
        }

        application.stop()
      }
    }

    "removeSettlor" must {

      def removeSettlor(settlorType: SettlorType): RemoveSettlor = RemoveSettlor(settlorType, index, date)

      "Return OK when the request is successful" in {

        forAll(arbitrarySettlorType) {
          settlorType =>

            val application = applicationBuilder()
              .configure(
                Seq(
                  "microservice.services.trusts.port" -> server.port(),
                  "auditing.enabled" -> false
                ): _*
              ).build()

            val connector = application.injector.instanceOf[TrustConnector]

            server.stubFor(
              put(urlEqualTo(removeSettlorUrl(utr)))
                .willReturn(ok)
            )

            val result = connector.removeSettlor(utr, removeSettlor(settlorType))

            result.futureValue.status mustBe OK

            application.stop()
        }
      }

      "return Bad Request when the request is unsuccessful" in {

        forAll(arbitrarySettlorType) {
          settlorType =>

            val application = applicationBuilder()
              .configure(
                Seq(
                  "microservice.services.trusts.port" -> server.port(),
                  "auditing.enabled" -> false
                ): _*
              ).build()

            val connector = application.injector.instanceOf[TrustConnector]

            server.stubFor(
              put(urlEqualTo(removeSettlorUrl(utr)))
                .willReturn(badRequest)
            )

            val result = connector.removeSettlor(utr, removeSettlor(settlorType))

            result.map(response => response.status mustBe BAD_REQUEST)

            application.stop()
        }
      }

    }
  }
}
