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

package controllers.individual.living

import config.annotations.LivingSettlor
import controllers.actions.StandardActionSets
import controllers.actions.individual.living.NameRequiredAction
import forms.CountryFormProvider
import models.Mode
import navigation.Navigator
import pages.individual.living.CountryOfResidencePage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptionsNonUK
import views.html.individual.living.CountryOfResidenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CountryOfResidenceController @Inject()(
                                              val controllerComponents: MessagesControllerComponents,
                                              standardActionSets: StandardActionSets,
                                              formProvider: CountryFormProvider,
                                              view: CountryOfResidenceView,
                                              repository: PlaybackRepository,
                                              @LivingSettlor navigator: Navigator,
                                              nameAction: NameRequiredAction,
                                              val countryOptions: CountryOptionsNonUK
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[String] = formProvider.withPrefix("livingSettlor.countryOfResidence")

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(CountryOfResidencePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, countryOptions.options, request.settlorName))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, countryOptions.options, request.settlorName))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CountryOfResidencePage, value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(CountryOfResidencePage, mode, updatedAnswers))
      )
  }
}
