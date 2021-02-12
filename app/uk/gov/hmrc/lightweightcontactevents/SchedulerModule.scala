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

package uk.gov.hmrc.lightweightcontactevents

import java.time.Clock

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton, Provider}
import play.api.inject._
import play.api.{Configuration, Environment, Logger}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.lightweightcontactevents.infrastructure.{DefaultRegularSchedule, VoaDataTransferExporter, VoaDataTransferLockKeeper, VoaDataTransferScheduler}

import scala.concurrent.ExecutionContext

class SchedulerModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    if (configuration.getOptional[String]("voaExport.enable").exists(_.toBoolean)) {
      Seq(
        bind[VoaDataTransferScheduler].toProvider[VoaDataTransferSchedulerProvider].eagerly(),
        bind[Clock].toInstance(Clock.systemUTC()).in[Singleton]
      )
    } else {
      Logger(getClass).warn("Export disabled, transfers won't be exported to VOA")
      Seq.empty[Binding[_]]
    }
  }
}

class VoaDataTransferSchedulerProvider @Inject()(actorSystem: ActorSystem, scheduler: DefaultRegularSchedule,
                                                 voaDataTransferExporter: VoaDataTransferExporter, reactiveMongoComponent: ReactiveMongoComponent,
                                                 lockKeeper: VoaDataTransferLockKeeper)(implicit ec: ExecutionContext) extends Provider[VoaDataTransferScheduler] {
  override def get(): VoaDataTransferScheduler = {
   val transferScheduler =  new VoaDataTransferScheduler(
      actorSystem.scheduler, actorSystem.eventStream,
      scheduler, voaDataTransferExporter, reactiveMongoComponent, lockKeeper)
    transferScheduler.start()
    transferScheduler
  }
}
