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

package views.individual.living

import controllers.individual.living.routes
import forms.UkAddressFormProvider
import models.{Name, NormalMode, UkAddress}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.UkAddressViewBehaviours
import views.html.individual.living.UkAddressView

class UkAddressViewSpec extends UkAddressViewBehaviours {

  val messageKeyPrefix = "livingSettlor.ukAddress"
  val name: Name = Name("First", Some("Middle"), "Last")

  override val form: Form[UkAddress] = new UkAddressFormProvider().apply()

  "UkAddressView" must {

    val view = viewFor[UkAddressView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, name.displayName, NormalMode)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name.displayName)

    behave like pageWithBackLink(applyView(form))

    behave like ukAddressPage(
      applyView,
      Some(messageKeyPrefix),
      routes.UkAddressController.onSubmit().url,
      name.displayName
    )

    behave like pageWithASubmitButton(applyView(form))
  }
}
