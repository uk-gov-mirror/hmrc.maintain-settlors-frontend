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

package controllers

import java.time.LocalDate

import base.SpecBase
import connectors.TrustConnector
import models.settlors.{DeceasedSettlor, IndividualSettlor, Settlors}
import models.{Name, TrustDetails, TypeOfTrust}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import pages.AdditionalSettlorsYesNoPage
import play.api.inject.bind
import play.api.libs.json.JsBoolean
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  "Index Controller" must {

    "redirect to task list when there are living settlors" in {

      val mockTrustConnector = mock[TrustConnector]

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(startDate = LocalDate.parse("2019-06-01"), typeOfTrust = TypeOfTrust.WillTrustOrIntestacyTrust, deedOfVariation = None)))

      when(mockTrustConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.successful(
            Settlors(
              settlor = List(IndividualSettlor(Name("Adam", None, "Test"), None, None, None, LocalDate.now, false)),
              settlorCompany = Nil,
              deceased = Some(DeceasedSettlor(
                None,
                Name("First", None, "Last"),
                None, None, None, None
              )
            )
          )
        ))

      when(mockTrustConnector.getIsDeceasedSettlorDateOfDeathRecorded(any())(any(), any()))
        .thenReturn(Future.successful(JsBoolean(true)))

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[TrustConnector].toInstance(mockTrustConnector)).build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad("UTRUTRUTR").url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.AddASettlorController.onPageLoad().url)

      application.stop()
    }

    "redirect to task list when there are no living settlors but user has previously answered yes to are there additional settlors to add to the trust" in {

      val mockTrustConnector = mock[TrustConnector]

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(startDate = LocalDate.parse("2019-06-01"), typeOfTrust = TypeOfTrust.WillTrustOrIntestacyTrust, deedOfVariation = None)))

      when(mockTrustConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.successful(
          Settlors(
            settlor = Nil,
            settlorCompany = Nil,
            deceased = Some(DeceasedSettlor(
              None,
              Name("First", None, "Last"),
              None, None, None, None
            )
            )
          )
        ))

      when(mockTrustConnector.getIsDeceasedSettlorDateOfDeathRecorded(any())(any(), any()))
        .thenReturn(Future.successful(JsBoolean(true)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(AdditionalSettlorsYesNoPage, true).success.value))
        .overrides(bind[TrustConnector].toInstance(mockTrustConnector)).build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad("UTRUTRUTR").url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.AddASettlorController.onPageLoad().url)

      application.stop()
    }

    "redirect to deceased settlor check answers when there are no living settlors" in {
      val mockTrustConnector = mock[TrustConnector]

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(startDate = LocalDate.parse("2019-06-01"), typeOfTrust = TypeOfTrust.WillTrustOrIntestacyTrust, deedOfVariation = None)))

      when(mockTrustConnector.getIsDeceasedSettlorDateOfDeathRecorded(any())(any(), any()))
        .thenReturn(Future.successful(JsBoolean(true)))

      when(mockTrustConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.successful(
          Settlors(
            settlor = Nil,
            settlorCompany = Nil,
            deceased = Some(DeceasedSettlor(
              None, Name("First", None, "Last"),
              None, None, None, None
            ))
          ))
        )

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
        .build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad("UTRUTRUTR").url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.individual.deceased.routes.CheckDetailsController.extractAndRender().url)

      application.stop()
    }
  }
}
