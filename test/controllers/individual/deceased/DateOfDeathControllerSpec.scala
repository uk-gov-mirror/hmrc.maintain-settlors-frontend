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

package controllers.individual.deceased

import java.time.{LocalDate, ZoneOffset}

import base.SpecBase
import config.annotations.DeceasedSettlor
import connectors.TrustConnector
import forms.DateOfDeathFormProvider
import models.{Name, TrustDetails}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.individual.deceased.{DateOfBirthPage, DateOfDeathPage, NamePage}
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository
import views.html.individual.deceased.DateOfDeathView

import scala.concurrent.Future

class DateOfDeathControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = LocalDate.now(ZoneOffset.UTC)
  val name = Name("FirstName", None, "LastName")
  val index: Int = 0
  val trustStartDate = LocalDate.parse("2019-02-03")

  val formProvider = new DateOfDeathFormProvider(frontendAppConfig)
  private def form = formProvider.withConfig("deceasedSettlor.dateOfDeath", trustStartDate)

  val mockTrustConnector = mock[TrustConnector]

  lazy val dateOfDeathRoute = routes.DateOfDeathController.onPageLoad().url

  val userAnswersWithName = emptyUserAnswers
    .set(NamePage, name).success.value

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, dateOfDeathRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, dateOfDeathRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "DateOfDeath Controller" must {

    "return OK and the correct view for a GET" in {

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(
          LocalDate.now,
          None,
          None,
          Some(true)
        )))

      val application = applicationBuilder(userAnswers = Some(userAnswersWithName))
        .overrides(
          bind[TrustConnector].toInstance(mockTrustConnector)
        )
        .build()

      val result = route(application, getRequest()).value

      val view = application.injector.instanceOf[DateOfDeathView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, name.displayName)(getRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(DateOfDeathPage, validAnswer).success.value
        .set(NamePage, name).success.value

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(
          LocalDate.now,
          None,
          None,
          Some(true)
        )))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TrustConnector].toInstance(mockTrustConnector)
        )
        .build()

      val view = application.injector.instanceOf[DateOfDeathView]

      val result = route(application, getRequest()).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), name.displayName)(getRequest(), messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val mockPlaybackRepository = mock[PlaybackRepository]

      when(mockPlaybackRepository.set(any())) thenReturn Future.successful(true)

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(
          LocalDate.now,
          None,
          None,
          Some(true)
        )))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].qualifiedWith(classOf[DeceasedSettlor]).toInstance(new FakeNavigator(onwardRoute)),
            bind[TrustConnector].toInstance(mockTrustConnector)
          )
          .build()

      val result = route(application, postRequest()).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(
          LocalDate.now,
          None,
          None,
          Some(true)
        )))

      val application = applicationBuilder(userAnswers = Some(userAnswersWithName))
        .overrides(
          bind[TrustConnector].toInstance(mockTrustConnector)
        )
        .build()

      val request =
        FakeRequest(POST, dateOfDeathRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[DateOfDeathView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, name.displayName)(request, messages).toString

      application.stop()
    }

    "return a Bad Request and errors when submitted date is before date of birth" in {

      val dateOfBirth = LocalDate.parse("2016-02-03")

      val submittedDate: LocalDate = LocalDate.parse("2013-02-03")

      val userAnswers = userAnswersWithName
        .set(DateOfBirthPage, dateOfBirth).success.value

      val form = formProvider.withConfig("deceasedSettlor.dateOfDeath", trustStartDate, (dateOfBirth, "beforeDateOfBirth"))

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(
          LocalDate.now,
          None,
          None,
          Some(true)
        )))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TrustConnector].toInstance(mockTrustConnector)
        )
        .build()

      val request =
        FakeRequest(POST, dateOfDeathRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> submittedDate.getDayOfMonth.toString,
            "value.month" -> submittedDate.getMonthValue.toString,
            "value.year"  -> submittedDate.getYear.toString
          )

      val boundForm = form.bind(Map(
        "value.day"   -> submittedDate.getDayOfMonth.toString,
        "value.month" -> submittedDate.getMonthValue.toString,
        "value.year"  -> submittedDate.getYear.toString
      ))

      val view = application.injector.instanceOf[DateOfDeathView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, name.displayName)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val result = route(application, getRequest()).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(
          LocalDate.now,
          None,
          None,
          Some(true)
        )))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[TrustConnector].toInstance(mockTrustConnector)
        ).build()

      val result = route(application, postRequest()).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
