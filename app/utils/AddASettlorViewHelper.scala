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

package utils

import models.settlors.{BusinessSettlor, DeceasedSettlor, IndividualSettlor, Settlors}
import play.api.i18n.Messages
import viewmodels.addAnother.{AddRow, AddToRows}

class AddASettlorViewHelper(settlors: Settlors)(implicit messages: Messages) {

  private def deceasedSettlorRow(settlor: DeceasedSettlor): AddRow = {
    AddRow(
      name = settlor.name.displayName,
      typeLabel = messages("entities.settlor.deceased"),
      changeLabel = messages("site.change.details"),
      changeUrl = Some(controllers.individual.deceased.routes.CheckDetailsController.extractAndRender().url),
      removeLabel =  Some(messages("site.delete")),
      removeUrl = None
    )
  }

  private def individualSettlorRow(settlor: IndividualSettlor, index: Int): AddRow = {
    AddRow(
      name = settlor.name.displayName,
      typeLabel = messages("entities.settlor.individual"),
      changeLabel = messages("site.change.details"),
      changeUrl = Some(controllers.individual.living.amend.routes.CheckDetailsController.extractAndRender(index).url),
      removeLabel =
        if (settlor.provisional) {
          Some(messages("site.delete"))
        } else {
          Some(messages("site.cannotRemove"))
        },
      removeUrl =
        if (settlor.provisional) {
          Some(controllers.individual.living.remove.routes.RemoveIndividualSettlorController.onPageLoad(index).url)
        } else {
          None
        }
    )
  }

  private def businessSettlorRow(settlor: BusinessSettlor, index: Int): AddRow = {
    AddRow(
      name = settlor.name,
      typeLabel = messages("entities.settlor.business"),
      changeLabel = messages("site.change.details"),
      changeUrl = Some(controllers.business.amend.routes.CheckDetailsController.extractAndRender(index).url),
      removeLabel =
        if (settlor.provisional) {
          Some(messages("site.delete"))
        } else {
          Some(messages("site.cannotRemove"))
        },
      removeUrl =
        if (settlor.provisional) {
          Some(controllers.business.remove.routes.RemoveBusinessSettlorController.onPageLoad(index).url)
        } else {
          None
        }
    )
  }

  def rows: AddToRows = {
    val complete =
      settlors.deceased.map(deceasedSettlorRow).toList ++
      settlors.settlor.zipWithIndex.map(x => individualSettlorRow(x._1, x._2)) ++
      settlors.settlorCompany.zipWithIndex.map(x => businessSettlorRow(x._1, x._2))

    AddToRows(Nil, complete)
  }

}
