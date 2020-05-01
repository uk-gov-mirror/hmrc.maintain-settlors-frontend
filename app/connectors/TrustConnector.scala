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

import config.FrontendAppConfig
import javax.inject.Inject
import models.settlors.{BusinessSettlor, DeceasedSettlor, IndividualSettlor, Settlors}
import models.{RemoveSettlor, TrustDetails}
import play.api.libs.json.{JsValue, Json, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class TrustConnector @Inject()(http: HttpClient, config : FrontendAppConfig) {

  private def getTrustDetailsUrl(utr: String) = s"${config.trustsUrl}/trusts/$utr/trust-details"

  def getTrustDetails(utr: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] = {
    http.GET[TrustDetails](getTrustDetailsUrl(utr))
  }

  private def getSettlorsUrl(utr: String) = s"${config.trustsUrl}/trusts/$utr/transformed/settlors"

  def getSettlors(utr: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[Settlors] = {
    http.GET[Settlors](getSettlorsUrl(utr))
  }

//  private def getDeceasedSettlorsUrl(utr: String) = s"${config.trustsUrl}/trusts/$utr/transformed/deceased-settlors"
//
//  def getDeceasedSettlors(utr: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeceasedSettlors] = {
//    http.GET[DeceasedSettlors](getDeceasedSettlorsUrl(utr))
//  }

  private def addIndividualSettlorUrl(utr: String) = s"${config.trustsUrl}/trusts/add-individual-settlor/$utr"

  private def addBusinessSettlorUrl(utr: String) = s"${config.trustsUrl}/trusts/add-business-settlor/$utr"

  def addIndividualSettlor(utr: String, settlor: IndividualSettlor)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    http.POST[JsValue, HttpResponse](addIndividualSettlorUrl(utr), Json.toJson(settlor))
  }

  def addBusinessSettlor(utr: String, settlor: BusinessSettlor)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    http.POST[JsValue, HttpResponse](addBusinessSettlorUrl(utr), Json.toJson(settlor))
  }

  private def amendIndividualSettlorUrl(utr: String, index: Int) = s"${config.trustsUrl}/trusts/amend-individual-settlor/$utr/$index"

  def amendIndividualSettlor(utr: String, index: Int, individual: IndividualSettlor)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    http.POST[JsValue, HttpResponse](amendIndividualSettlorUrl(utr, index), Json.toJson(individual))(implicitly[Writes[JsValue]], HttpReads.readRaw, hc, ec)
  }

  private def amendDeceasedSettlorUrl(utr: String) = s"${config.trustsUrl}/trusts/amend-deceased-settlor/$utr"

  def amendDeceasedSettlor(utr: String, deceasedSettlor: DeceasedSettlor)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    http.POST[JsValue, HttpResponse](amendDeceasedSettlorUrl(utr), Json.toJson(deceasedSettlor))(implicitly[Writes[JsValue]], HttpReads.readRaw, hc, ec)
  }

  private def amendBusinessSettlorUrl(utr: String, index: Int) = s"${config.trustsUrl}/trusts/amend-business-settlor/$utr/$index"

  def amendBusinessSettlor(utr: String, index: Int, business: BusinessSettlor)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    http.POST[JsValue, HttpResponse](amendBusinessSettlorUrl(utr, index), Json.toJson(business))(implicitly[Writes[JsValue]], HttpReads.readRaw, hc, ec)
  }

  private def removeSettlorUrl(utr: String) = s"${config.trustsUrl}/trusts/$utr/settlors/remove"

  def removeSettlor(utr: String, settlor: RemoveSettlor)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    http.PUT[JsValue, HttpResponse](removeSettlorUrl(utr), Json.toJson(settlor))
  }
}
