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
import akka.util.Timeout
import play.api.Logger
import uk.gov.hmrc.mongo.lock.LockService

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

object LockedJobScheduler {
  implicit val timeout: Timeout = Timeout(1 hour)
}

abstract class LockedJobScheduler[Event <: AnyRef](scheduler: Scheduler, eventStream: EventStream) {

  val name: String
  val schedule: Schedule

  protected def lockService: LockService

  protected def logger: Logger

  def runJob()(implicit ec: ExecutionContext): Future[Event]

  def start()(implicit ec: ExecutionContext): Unit = {
    scheduleNextImport()
  }

  private def run()(implicit ec: ExecutionContext) = {
    logger.info(s"Starting job: $name")
    runJob().map {
      eventStream.publish
    } recoverWith {
      case e: Exception =>
        logger.error(s"Error running job: $name", e)
        Future.failed(e)
    }
  }

  private def scheduleNextImport()(implicit ec: ExecutionContext): Unit = {
    val delay = schedule.timeUntilNextRun()
    logger.info(s"Scheduling $name to run in: $delay")
    scheduler.scheduleOnce(delay) {
      lockService.withLock {
        run()
      } onComplete { _ => scheduleNextImport() }
    }
  }

}
