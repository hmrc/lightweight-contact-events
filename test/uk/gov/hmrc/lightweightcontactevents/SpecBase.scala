/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.lightweightcontactevents

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.*
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.lightweightcontactevents.models.QueuedDataTransfer
import uk.gov.hmrc.lightweightcontactevents.repository.QueuedDataTransferRepository

import java.net.URL
import scala.concurrent.Future

trait SpecBase extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar:

  def injector: Injector = app.injector

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  def messages: Messages = messagesApi.preferred(fakeRequest)

  def getHttpMock(returnedStatus: Int): HttpClientV2 =
    val httpClientV2Mock = mock[HttpClientV2]
    when(
      httpClientV2Mock.post(any[URL])(using any[HeaderCarrier])
    ).thenReturn(RequestBuilderStub(Right(returnedStatus), "{}"))
    when(
      httpClientV2Mock.get(any[URL])(using any[HeaderCarrier])
    ).thenReturn(RequestBuilderStub(Right(returnedStatus), "{}"))
    httpClientV2Mock

  def getQueuedDataTransferRepository: QueuedDataTransferRepository =
    val repositoryMock = mock[QueuedDataTransferRepository]
    when(repositoryMock.insert(any[QueuedDataTransfer])).thenReturn(Future.unit)
    repositoryMock
