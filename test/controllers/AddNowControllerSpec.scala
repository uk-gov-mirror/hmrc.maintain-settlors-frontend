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
import forms.AddSettlorTypeFormProvider
import models.NormalMode
import models.settlors.{Settlors, TypeOfSettlorToAdd}
import models.settlors.TypeOfSettlorToAdd.{Business, Individual, prefix}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AddNowPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import viewmodels.RadioOption
import views.html.AddNowView

import scala.concurrent.Future

class AddNowControllerSpec extends SpecBase with MockitoSugar {

  val form: Form[TypeOfSettlorToAdd] = new AddSettlorTypeFormProvider()()
  lazy val addNowRoute: String = routes.AddNowController.onPageLoad().url
  val individualSettlorAnswer: TypeOfSettlorToAdd.Individual.type = TypeOfSettlorToAdd.Individual
  val mockTrustService: TrustService = mock[TrustService]

  when(mockTrustService.getSettlors(any())(any(), any()))
    .thenReturn(Future.successful(Settlors(Nil, Nil, None)))

  val values: List[TypeOfSettlorToAdd] = List(
    Individual, Business
  )

  val options: List[RadioOption] = values.map {
    value =>
      RadioOption(prefix, value.toString)
  }

  "AddNow Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TrustService].toInstance(mockTrustService)
        ).build()

      val request = FakeRequest(GET, addNowRoute)

      val view = application.injector.instanceOf[AddNowView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, options)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val answers = emptyUserAnswers.set(AddNowPage, individualSettlorAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[TrustService].toInstance(mockTrustService)
        ).build()

      val request = FakeRequest(GET, addNowRoute)

      val view = application.injector.instanceOf[AddNowView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(individualSettlorAnswer), options)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when Individual settlor is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TrustService].toInstance(mockTrustService)
        ).build()

      val request =
        FakeRequest(POST, addNowRoute)
          .withFormUrlEncodedBody(("value", TypeOfSettlorToAdd.Individual.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.individual.living.routes.NameController.onPageLoad(NormalMode).url

      application.stop()
    }

    "redirect to the next page when Business settlor is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TrustService].toInstance(mockTrustService)
        ).build()

      val request =
        FakeRequest(POST, addNowRoute)
          .withFormUrlEncodedBody(("value", TypeOfSettlorToAdd.Business.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.FeatureNotAvailableController.onPageLoad().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TrustService].toInstance(mockTrustService)
        ).build()

      val request = FakeRequest(POST, addNowRoute)

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[AddNowView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, options)(fakeRequest, messages).toString

       application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[TrustService].toInstance(mockTrustService)
        ).build()

      val request = FakeRequest(GET, addNowRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[TrustService].toInstance(mockTrustService)
        ).build()

      val request =
        FakeRequest(POST, addNowRoute)
          .withFormUrlEncodedBody(("value", individualSettlorAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
