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
import javax.inject.{Inject, Singleton}
import org.joda.time.Duration
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.lock.{LockKeeper, LockRepository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VoaDataTransferScheduler @Inject() (scheduler: Scheduler, eventStream: EventStream,
                                          val schedule: DefaultRegularSchedule, voaDataTransferExporter: VoaDataTransferExporter,
                                          reactiveMongoComponent: ReactiveMongoComponent, lock: VoaDataTransferLockKeeper
                                         )(implicit val ec: ExecutionContext)

  extends LockedJobScheduler[ExportEvent](lock, scheduler, eventStream) {


  override val name: String = this.getClass.getName

  override def runJob()(implicit ec: ExecutionContext): Future[ExportEvent] = {
    voaDataTransferExporter.exportBatch().map(_ => ExportSucess).recover {
      case ex: Exception => ExportFailed
    }

    //Future.successful(ExportSucess)
  }
}

@Singleton
class VoaDataTransferLockKeeper @Inject()(reactiveMongoComponent: ReactiveMongoComponent) extends LockKeeper {

  val lockRepository =  {
    implicit val db = reactiveMongoComponent.mongoConnector.db
    new LockRepository()
  }

  override def lockId: String = this.getClass.getName

  override val forceLockReleaseAfter: Duration = Duration.standardHours(1)

  override def repo: LockRepository = lockRepository
}
