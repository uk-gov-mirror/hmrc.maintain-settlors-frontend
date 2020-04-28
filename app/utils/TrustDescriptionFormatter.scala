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

package utils

import models.DeedOfVariation.AdditionToWillTrust
import models.{DeedOfVariation, TypeOfTrust}

trait TrustDescriptionFormatter {
  
  private val prefix = "trustDescription"

  def getTrustDescription(typeOfTrust: TypeOfTrust, deedOfVariation: Option[DeedOfVariation]): String = {
    (typeOfTrust, deedOfVariation) match {
      case (TypeOfTrust.WillTrustOrIntestacyTrust, _) => s"$prefix.willTrust"
      case (TypeOfTrust.IntervivosSettlementTrust, _) => s"$prefix.intervivosTrust"
      case (TypeOfTrust.DeedOfVariation, Some(AdditionToWillTrust)) => s"$prefix.deedOfVariationInAdditionToWill"
      case (TypeOfTrust.DeedOfVariation, _) => s"$prefix.deedOfVariation"
      case (TypeOfTrust.EmployeeRelated, _) => s"$prefix.employeeRelated"
      case (TypeOfTrust.FlatManagementTrust, _) => s"$prefix.flatManagementTrust"
      case (TypeOfTrust.HeritageTrust, _) => s"$prefix.heritageTrust"
    }
  }

}
