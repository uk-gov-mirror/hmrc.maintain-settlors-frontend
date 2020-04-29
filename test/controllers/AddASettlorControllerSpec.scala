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
import connectors.TrustStoreConnector
import forms.AddASettlorFormProvider
import models.settlors.{BusinessSettlor, IndividualSettlor, Settlors}
import models.{AddASettlor, CompanyType, Name, NationalInsuranceNumber, RemoveSettlor}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.AddASettlorViewHelper
import viewmodels.addAnother.AddRow
import views.html.{AddASettlorView, MaxedOutSettlorsView}

import scala.concurrent.{ExecutionContext, Future}

class AddASettlorControllerSpec extends SpecBase with ScalaFutures {

  lazy val getRoute : String = controllers.routes.AddASettlorController.onPageLoad().url
  lazy val submitRoute : String = controllers.routes.AddASettlorController.submit().url
  lazy val submitCompleteRoute : String = controllers.routes.AddASettlorController.submitComplete().url

  val mockStoreConnector : TrustStoreConnector = mock[TrustStoreConnector]

  val addTrusteeForm = new AddASettlorFormProvider()()

  private def individualSettlor(provisional: Boolean) = IndividualSettlor(
    name = Name(firstName = "First", middleName = None, lastName = "Last"),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    identification = Some(NationalInsuranceNumber("JS123456A")),
    address = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = provisional
  )

  private def businessSettlor(provisional: Boolean) = BusinessSettlor(
    name = "Humanitarian Company Ltd",
    companyType = Some(CompanyType.Investment),
    companyTime = Some(false),
    utr = None,
    address = None,
    entityStart = LocalDate.parse("2012-03-14"),
    provisional = provisional
  )

  private val settlors = Settlors(
    List(individualSettlor(true)),
    List(businessSettlor(true))
  )

  lazy val featureNotAvailable : String = controllers.routes.FeatureNotAvailableController.onPageLoad().url

  val settlorRows = List(
    AddRow("First Last", typeLabel = "Individual settlor", "Change details", Some(controllers.individual.living.amend.routes.CheckDetailsController.extractAndRender(0).url), "Remove", Some(controllers.individual.living.remove.routes.RemoveIndividualSettlorController.onPageLoad(0).url)),
    AddRow("Humanitarian Company Ltd", typeLabel = "Business settlor", "Change details", Some(controllers.business.amend.routes.CheckDetailsController.extractAndRender(0).url), "Remove", Some(controllers.business.remove.routes.RemoveBusinessSettlorController.onPageLoad(0).url))
  )

  class FakeService(data: Settlors) extends TrustService {

    override def getSettlors(utr: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Settlors] = Future.successful(data)

    override def getIndividualSettlor(utr: String, index: Int)
                                         (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[IndividualSettlor] =
      Future.successful(individualSettlor(true))

    override def getBusinessSettlor(utr: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[BusinessSettlor] =
      Future.successful(businessSettlor(true))

    override def removeSettlor(utr: String, settlor: RemoveSettlor)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
      Future.successful(HttpResponse(OK))
  }

  "AddASettlor Controller" when {

    "no data" must {

      "redirect to Session Expired for a GET if no existing data is found" in {

        val fakeService = new FakeService(Settlors(Nil, Nil))

        val application = applicationBuilder(userAnswers = None).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

      "redirect to Session Expired for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request =
          FakeRequest(POST, submitRoute)
            .withFormUrlEncodedBody(("value", AddASettlor.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }
    }

    "there are settlors" must {

      "return OK and the correct view for a GET" in {

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddASettlorView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(addTrusteeForm, Nil, settlorRows, "The trust has 2 settlors", Nil)(fakeRequest, messages).toString

        application.stop()
      }

      "redirect to the maintain task list when the user says they are done" in {

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService),
          bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector)
        )).build()

        val request =
          FakeRequest(POST, submitRoute)
            .withFormUrlEncodedBody(("value", AddASettlor.NoComplete.toString))

        when(mockStoreConnector.setTaskComplete(any())(any(), any())).thenReturn(Future.successful(HttpResponse.apply(200)))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()
      }

      "redirect to the maintain task list when the user says they want to add later" ignore {

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request =
          FakeRequest(POST, submitRoute)
            .withFormUrlEncodedBody(("value", AddASettlor.YesLater.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request =
          FakeRequest(POST, submitRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = addTrusteeForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddASettlorView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual view(boundForm, Nil, settlorRows, "The trust has 2 settlors", Nil)(fakeRequest, messages).toString

        application.stop()
      }
    }

    "maxed out settlors" must {

      val settlors = Settlors(
        List.fill(25)(individualSettlor(true)),
        List.fill(25)(businessSettlor(true))
      )

      val fakeService = new FakeService(settlors)

      val settlorRows = new AddASettlorViewHelper(settlors).rows

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MaxedOutSettlorsView]

        status(result) mustEqual OK

        val content = contentAsString(result)

        content mustEqual view(settlorRows.inProgress, settlorRows.complete, "The trust has 50 settlors")(fakeRequest, messages).toString
        content must include("You cannot enter another settlor as you have entered a maximum of 50.")
        content must include("If you have further settlors to add, write to HMRC with their details.")

        application.stop()

      }

      "return correct view when one type of settlor is maxed out" in {

        val settlors = Settlors(
          List.fill(25)(individualSettlor(true)),
          Nil
        )

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        contentAsString(result) must include("You cannot add another individual as you have entered a maximum of 25.")
        contentAsString(result) must include("If you have further settlors to add within this type, write to HMRC with their details.")

        application.stop()

      }

      "redirect to add to page and set settlors to complete when user clicks continue" in {

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService),
          bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector)
        )).build()

        val request = FakeRequest(POST, submitCompleteRoute)

        when(mockStoreConnector.setTaskComplete(any())(any(), any())).thenReturn(Future.successful(HttpResponse.apply(200)))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()

      }

    }

    "no provisional settlors" must {

      val settlors = Settlors(
        List(individualSettlor(false)),
        List(businessSettlor(false))
      )

      val settlorRows = List(
        AddRow("First Last", typeLabel = "Individual settlor", "Change details", Some(controllers.individual.living.amend.routes.CheckDetailsController.extractAndRender(0).url), "Remove", None),
        AddRow("Humanitarian Company Ltd", typeLabel = "Business settlor", "Change details", Some(controllers.business.amend.routes.CheckDetailsController.extractAndRender(0).url), "Remove", None)
      )

      "return OK and the correct view for a GET with no remove links" in {

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddASettlorView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(addTrusteeForm, Nil, settlorRows, "The trust has 2 settlors", Nil)(fakeRequest, messages).toString

        application.stop()
      }
    }
  }
}
