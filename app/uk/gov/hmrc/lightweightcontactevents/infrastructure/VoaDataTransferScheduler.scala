/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.lightweightcontactevents.infrastructure

import akka.actor.Scheduler
import akka.event.EventStream
import play.api.Logger
import uk.gov.hmrc.lightweightcontactevents.infrastructure.LockedJobScheduler.timeout
import uk.gov.hmrc.mongo.lock.{LockService, MongoLockRepository}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VoaDataTransferScheduler @Inject()(scheduler: Scheduler,
                                         eventStream: EventStream,
                                         val schedule: DefaultRegularSchedule,
                                         voaDataTransferExporter: VoaDataTransferExporter,
                                         mongoLockRepository: MongoLockRepository
                                        )(implicit val ec: ExecutionContext)

  extends LockedJobScheduler[ExportEvent](scheduler, eventStream) {

  override protected val lockService: LockService = LockService(mongoLockRepository, "VoaDataTransferLock", timeout.duration)
  override protected val logger: Logger = Logger(getClass)

  override val name: String = this.getClass.getName

  override def runJob()(implicit ec: ExecutionContext): Future[ExportEvent] =
    voaDataTransferExporter.exportBatch()
      .map(_ => ExportSuccess)
      .recover {
        case ex: Exception =>
          logger.error("Export failed", ex)
          ExportFailed
      }

}
