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

package controllers.individual.living.add

import java.time.LocalDate

import base.SpecBase
import config.annotations.LivingSettlor
import forms.IdCardDetailsFormProvider
import models.{IdCard, Name, UserAnswers}
import navigation.Navigator
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.individual.living.{IdCardDetailsPage, NamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository
import utils.InputOption
import utils.countryOptions.CountryOptions
import views.html.individual.living.add.IdCardDetailsView

import scala.concurrent.Future

class IdCardDetailsControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new IdCardDetailsFormProvider(frontendAppConfig)
  private def form = formProvider.withPrefix("livingSettlor")

  def onwardRoute: Call = Call("GET", "/foo")
  val name: Name = Name("FirstName", None, "LastName")

  override val emptyUserAnswers: UserAnswers = UserAnswers("id", "UTRUTRUTR", LocalDate.now(), None, None, isDateOfDeathRecorded = true)
    .set(NamePage, name).success.value

  val idCardDetailsRoute: String = routes.IdCardDetailsController.onPageLoad().url

  val getRequest = FakeRequest(GET, idCardDetailsRoute)

  val countryOptions: Seq[InputOption] = app.injector.instanceOf[CountryOptions].options

  val validData: IdCard = IdCard("country", "card number", LocalDate.of(2020, 1, 1))

  "IdCardDetails Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val result = route(application, getRequest).value

      val view = application.injector.instanceOf[IdCardDetailsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, countryOptions, name.displayName)(getRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(NamePage, name).success.value
        .set(IdCardDetailsPage, validData).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val view = application.injector.instanceOf[IdCardDetailsView]

      val result = route(application, getRequest).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validData), countryOptions, name.displayName)(getRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val mockPlaybackRepository = mock[PlaybackRepository]

      when(mockPlaybackRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].qualifiedWith(classOf[LivingSettlor]).toInstance(fakeNavigator))

          .build()

      val request =
        FakeRequest(POST, idCardDetailsRoute)
          .withFormUrlEncodedBody(
            "country" -> "country",
            "number" -> "123456",
            "expiryDate.day"   -> validData.expirationDate.getDayOfMonth.toString,
            "expiryDate.month" -> validData.expirationDate.getMonthValue.toString,
            "expiryDate.year"  -> validData.expirationDate.getYear.toString
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, idCardDetailsRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[IdCardDetailsView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, countryOptions, name.displayName)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val result = route(application, getRequest).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, idCardDetailsRoute)
          .withFormUrlEncodedBody(
            "country" -> "country",
            "number" -> "123456",
            "expiryDate.day"   -> validData.expirationDate.getDayOfMonth.toString,
            "expiryDate.month" -> validData.expirationDate.getMonthValue.toString,
            "expiryDate.year"  -> validData.expirationDate.getYear.toString
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
