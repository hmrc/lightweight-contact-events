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

package uk.gov.hmrc.lightweightcontactevents

import org.apache.pekko.actor.ActorSystem
import play.api.inject._
import play.api.{Configuration, Environment, Logging}
import uk.gov.hmrc.lightweightcontactevents.infrastructure._
import uk.gov.hmrc.mongo.lock.MongoLockRepository

import java.time.Clock
import javax.inject.{Inject, Provider, Singleton}
import scala.concurrent.ExecutionContext

class SchedulerModule extends Module with Logging {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    if (configuration.getOptional[String]("voaExport.enable").exists(_.toBoolean)) {
      Seq(
        bind[VoaDataTransferScheduler].toProvider[VoaDataTransferSchedulerProvider].eagerly(),
        bind[Clock].toInstance(Clock.systemUTC()).in[Singleton]
      )
    } else {
      logger.warn("Export disabled, transfers won't be exported to VOA")
      Seq.empty[Binding[_]]
    }
  }
}

class VoaDataTransferSchedulerProvider @Inject()(actorSystem: ActorSystem,
                                                 schedule: DefaultRegularSchedule,
                                                 voaDataTransferExporter: VoaDataTransferExporter,
                                                 mongoLockRepository: MongoLockRepository
                                                )(implicit ec: ExecutionContext)
  extends Provider[VoaDataTransferScheduler] {

  override def get(): VoaDataTransferScheduler = {
    val transferScheduler = new VoaDataTransferScheduler(
      actorSystem.scheduler,
      actorSystem.eventStream,
      schedule,
      voaDataTransferExporter,
      mongoLockRepository
    )
    transferScheduler.start()
    transferScheduler
  }

}
