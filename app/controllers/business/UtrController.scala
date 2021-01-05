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

package controllers.business

import config.annotations.BusinessSettlor
import controllers.actions.StandardActionSets
import controllers.actions.business.NameRequiredAction
import forms.UtrFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.business.UtrPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.business.UtrView

import scala.concurrent.{ExecutionContext, Future}

class UtrController @Inject()(
                               val controllerComponents: MessagesControllerComponents,
                               standardActionSets: StandardActionSets,
                               nameAction: NameRequiredAction,
                               formProvider: UtrFormProvider,
                               playbackRepository: PlaybackRepository,
                               view: UtrView,
                               @BusinessSettlor navigator: Navigator
                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[String] = formProvider.withPrefix("businessSettlor.utr")

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(UtrPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.settlorName, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, request.settlorName, mode))),
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(UtrPage, value))
            _ <- playbackRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(UtrPage, mode, updatedAnswers, updatedAnswers.trustType))
        }
      )
  }
}
