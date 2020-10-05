package uk.gov.hmrc.lightweightcontactevents.repository

import java.time.Instant

import org.scalatest.OptionValues
import uk.gov.hmrc.lightweightcontactevents.DiAcceptanceTest
import uk.gov.hmrc.lightweightcontactevents.models.{ConfirmedContactDetails, PropertyAddress, QueuedDataTransfer, VOADataTransfer}

import scala.concurrent.ExecutionContext.Implicits.global

class QueuedDataTransferRepositorySpec extends DiAcceptanceTest with OptionValues {

  override def testDbPrefix(): String = "cf-repository-spec"

  def mongoRepository() = app.injector.instanceOf[QueuedDataTransferRepository]

  "Repository" should {
    "save item to DB and read it back" in {

      val item = aQueuedDataTransfer()
      await(mongoRepository().insert(item))

      val itemFromDb = await(mongoRepository().findById(item.id)).get

      Console.println(itemFromDb)

      itemFromDb mustBe (item)

    }

    "Update firstError time" in {
      val item = aQueuedDataTransfer()
      await(mongoRepository().insert(item))

      val errorTime = Instant.now()

      await(mongoRepository().updateTime(item.id, errorTime))

      val itemFromDatabase = await(mongoRepository().findById(item.id))

      itemFromDatabase.value.fistError.value mustBe(errorTime)

    }

    "Get batch of elements" in {
      val items = (1 to 20).map(_ => aQueuedDataTransfer()).toList

      await(mongoRepository().removeAll())

      await(mongoRepository().bulkInsert(items))

      val res = await(mongoRepository().findBatch())

      res must have size 10

      items must contain allElementsOf(res)

    }


  }



  def aQueuedDataTransfer() = {
    QueuedDataTransfer(aVoaDataTransfer())
  }

  def aVoaDataTransfer() = {
    VOADataTransfer(aConfirmedContactDetails(), aPropertyAddress(), true,
    "Subject", "email@email.com", "category", "subCategory", "Free text message")
  }

  def aPropertyAddress() = {
    PropertyAddress("Some stree", None, "Some town", Some("Some county"), "BN12 4AX")
  }


  def aConfirmedContactDetails()  = {
    ConfirmedContactDetails(
      "John",
      "Doe",
      "email@noreply.voa.gov.uk",
      "0123456789"
    )
  }


}
