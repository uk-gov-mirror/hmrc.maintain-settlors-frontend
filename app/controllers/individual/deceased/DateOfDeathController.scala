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

package controllers.individual.deceased

import config.annotations.DeceasedSettlor
import controllers.actions.StandardActionSets
import controllers.actions.individual.deceased.NameRequiredAction
import forms.DateOfDeathFormProvider
import javax.inject.Inject
import navigation.Navigator
import pages.individual.deceased.DateOfDeathPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.individual.deceased.DateOfDeathView

import scala.concurrent.{ExecutionContext, Future}

class DateOfDeathController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: PlaybackRepository,
                                       @DeceasedSettlor navigator: Navigator,
                                       standardActionSets: StandardActionSets,
                                       nameAction: NameRequiredAction,
                                       formProvider: DateOfDeathFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: DateOfDeathView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider.withPrefix("deceasedSettlor.dateOfDeath")


  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(DateOfDeathPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.settlorName))
  }

  def onSubmit(): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, request.settlorName))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(DateOfDeathPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(DateOfDeathPage, updatedAnswers))
      )
  }
}
