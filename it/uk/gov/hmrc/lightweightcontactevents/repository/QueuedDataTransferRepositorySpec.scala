package uk.gov.hmrc.lightweightcontactevents.repository

import uk.gov.hmrc.lightweightcontactevents.DiAcceptanceTest
import uk.gov.hmrc.lightweightcontactevents.models.{ConfirmedContactDetails, Contact, PropertyAddress, QueuedDataTransfer, VOADataTransfer}

import scala.concurrent.ExecutionContext.Implicits.global

class QueuedDataTransferRepositorySpec extends DiAcceptanceTest {

  override def testDbPrefix(): String = "cf-repository-spec"

  def mongoRepository() = app.injector.instanceOf[QueuedDataTransferRepository]

  "Repository" should {
    "save item to DB and read it back" in {

      val item = aQueuedDataTransfer()
      val insertResult = await(mongoRepository().insert(item))

      val itemFromDb = await(mongoRepository().findById(item.id)).get

      Console.println(itemFromDb)

      itemFromDb mustBe (item)

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
