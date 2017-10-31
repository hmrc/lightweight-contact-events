/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.lightweightcontactevents.models

import play.api.libs.json.Json

case class Email(to: Seq[String], templateId: String, parameters: Map[String, String])

object Email {
  implicit val writer = Json.writes[Email]

  def apply(ctc: Contact): Email = {
    val parameters = Map(
      "firstName" -> ctc.contact.firstName,
      "lastName" -> ctc.contact.lastName,
      "email" -> ctc.contact.email,
      "contactNumber" -> ctc.contact.contactNumber,
      "addressLine1" -> ctc.propertyAddress.addressLine1,
      "town" -> ctc.propertyAddress.town,
      "postcode" -> ctc.propertyAddress.postcode,
      "enquiryCategoryMsg" -> ctc.enquiryCategoryMsg,
      "subEnquiryCategoryMsg" -> ctc.subEnquiryCategoryMsg,
      "message" -> ctc.message
    ) ++
      (ctc.propertyAddress.addressLine2 match {
        case Some(addr) => Map("addressLine2" -> addr)
        case None => Map()
      }) ++
      (ctc.propertyAddress.county match {
        case Some(cty) => Map("county" -> cty)
        case None => Map()
      })
    Email(Seq(), "", parameters)
  }
}

