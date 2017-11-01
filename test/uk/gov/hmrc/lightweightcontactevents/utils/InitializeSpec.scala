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

package uk.gov.hmrc.lightweightcontactevents.utils

import java.util

import com.typesafe.config
import com.typesafe.config.ConfigFactory
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.lightweightcontactevents.SpecBase
import scala.collection.JavaConversions._

class InitializeSpec extends SpecBase with MockitoSugar {
   "Initialize" must {
     "load the council tax email address and the business rates email address from the configuration" in {
       val sm = Map("email.council-tax" -> "ct@voa.gov.uk", "email.non-domestic-rates" -> "ndr@voa.gov.uk")
       val conf = new Configuration(ConfigFactory.parseMap(sm))
       val initialize = new Initialize(conf)
       initialize.councilTaxEmail mustBe "ct@voa.gov.uk"
     }

     "throw an exception if the council tax email address is not configured" in {
       val sm = Map("email.non-domestic-rates" -> "ndr@voa.gov.uk")
       val conf = new Configuration(ConfigFactory.parseMap(sm))

       intercept[Exception] {
       val initialize = new Initialize(conf)
       }
     }

     "throw an exception if the business rates email address is not configured" in {
       val sm = Map("email.council-tax" -> "ct@voa.gov.uk")
       val conf = new Configuration(ConfigFactory.parseMap(sm))

       intercept[Exception] {
         val initialize = new Initialize(conf)
       }
     }
   }
}
