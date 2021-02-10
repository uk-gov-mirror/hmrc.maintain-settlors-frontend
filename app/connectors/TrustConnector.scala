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

import config.FrontendAppConfig
import javax.inject.Inject
import models.settlors.{BusinessSettlor, DeceasedSettlor, IndividualSettlor, Settlors}
import models.{RemoveSettlor, TrustDetails}
import play.api.libs.json.{JsBoolean, JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits.{readFromJson, readRaw}

import scala.concurrent.{ExecutionContext, Future}

class TrustConnector @Inject()(http: HttpClient, config: FrontendAppConfig) {

  private val trustsUrl: String = s"${config.trustsUrl}/trusts"
  private val settlorsUrl: String = s"$trustsUrl/settlors"

  def getTrustDetails(utr: String)
                     (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] = {
    val url: String = s"$trustsUrl/$utr/trust-details"
    http.GET[TrustDetails](url)
  }

  def getSettlors(utr: String)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Settlors] = {
    val url: String = s"$settlorsUrl/$utr/transformed"
    http.GET[Settlors](url)
  }

  def getIsDeceasedSettlorDateOfDeathRecorded(utr: String)
                                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsBoolean] = {
    val url: String = s"$settlorsUrl/$utr/transformed/deceased-settlor-death-recorded"
    http.GET[JsBoolean](url)
  }

  def addIndividualSettlor(utr: String, settlor: IndividualSettlor)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$settlorsUrl/add-individual/$utr"
    http.POST[JsValue, HttpResponse](url, Json.toJson(settlor))
  }

  def amendIndividualSettlor(utr: String, index: Int, individual: IndividualSettlor)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$settlorsUrl/amend-individual/$utr/$index"
    http.POST[JsValue, HttpResponse](url, Json.toJson(individual))
  }

  def addBusinessSettlor(utr: String, settlor: BusinessSettlor)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$settlorsUrl/add-business/$utr"
    http.POST[JsValue, HttpResponse](url, Json.toJson(settlor))
  }

  def amendBusinessSettlor(utr: String, index: Int, business: BusinessSettlor)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$settlorsUrl/amend-business/$utr/$index"
    http.POST[JsValue, HttpResponse](url, Json.toJson(business))
  }

  def amendDeceasedSettlor(utr: String, deceasedSettlor: DeceasedSettlor)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$settlorsUrl/amend-deceased/$utr"
    http.POST[JsValue, HttpResponse](url, Json.toJson(deceasedSettlor))
  }

  def removeSettlor(utr: String, settlor: RemoveSettlor)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$settlorsUrl/$utr/remove"
    http.PUT[JsValue, HttpResponse](url, Json.toJson(settlor))
  }
}
