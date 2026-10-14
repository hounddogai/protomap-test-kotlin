package com.acme.transport.ops.linehaulimporter

import com.acme.datasets.v1.DatasetServiceGrpcKt
import com.acme.datasets.v1.getDatasetRequest
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.getNotificationRequest
import com.acme.sponsorships.v1.SponsorCalculationServiceGrpcKt
import com.acme.sponsorships.v1.getSponsorRatesRequest
import com.acme.telematics.v1.CountGeoFenceTotalsRequestKt
import com.acme.telematics.v1.GeoFenceReportingServiceGrpcKt
import com.acme.telematics.v1.countGeoFenceTotalsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.notifications.v1.NotificationService/GetNotification and hands what it keeps to the sink of its kind. */
suspend fun copyNotificationId(channel: ManagedChannel) {
    val reader = NotificationServiceGrpcKt.NotificationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { reader.getNotification(getNotificationRequest { this.notificationId = "primary-copy-notification-id" }) }
    val record = CopyNotificationIdRecord(
        correlationId = got.result.correlationId,
        message = got.result.message,
        status = got.result.status,
    )
    dispatchCopyNotificationId("geo-fence-reporting", record, channel)
}

/** Where copyNotificationId hands each record. */
interface CopyNotificationIdDestination {
    suspend fun forward(rec: CopyNotificationIdRecord)
}

/** What copyNotificationId keeps of each GetNotification response. */
data class CopyNotificationIdRecord(val correlationId: String, val message: String, val status: String)

/** Sends records to acme.sponsorships.v1.SponsorCalculationService. */
class CopyNotificationIdSponsorCalculation(private val stub: SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub) : CopyNotificationIdDestination {
    override suspend fun forward(rec: CopyNotificationIdRecord) {
        RpcBoundary.run("acme.sponsorships.v1.SponsorCalculationService/GetSponsorRates") { stub.getSponsorRates(getSponsorRatesRequest { this.sponsorId = rec.message }) }
    }
}

/** Hands records to acme.telematics.v1.GeoFenceReportingService. */
class CopyNotificationIdGeoFenceReporting(private val stub: GeoFenceReportingServiceGrpcKt.GeoFenceReportingServiceCoroutineStub) : CopyNotificationIdDestination {
    override suspend fun forward(rec: CopyNotificationIdRecord) {
        val request = countGeoFenceTotalsRequest {
            this.filter = CountGeoFenceTotalsRequestKt.filter { this.ownerId = rec.status }
        }
        stub.countGeoFenceTotals(request)
    }
}

/** Sends records to acme.datasets.v1.DatasetService. */
class CopyNotificationIdDataset(private val stub: DatasetServiceGrpcKt.DatasetServiceCoroutineStub) : CopyNotificationIdDestination {
    override suspend fun forward(rec: CopyNotificationIdRecord) {
        stub.getDataset(getDatasetRequest { this.datasetId = rec.status })
    }
}

/** Only logs, for kinds no service takes yet. */
class CopyNotificationIdLogOnly : CopyNotificationIdDestination {
    override suspend fun forward(rec: CopyNotificationIdRecord) {
        LoggerFactory.getLogger("CopyNotificationIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchCopyNotificationId(kind: String, rec: CopyNotificationIdRecord, channel: ManagedChannel) {
    var write: suspend (CopyNotificationIdRecord) -> Unit = CopyNotificationIdLogOnly()::forward
    when (kind) {
        "sponsor-calculation" -> write = CopyNotificationIdSponsorCalculation(SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub(channel))::forward
        "geo-fence-reporting" -> write = CopyNotificationIdGeoFenceReporting(GeoFenceReportingServiceGrpcKt.GeoFenceReportingServiceCoroutineStub(channel))::forward
        "dataset" -> write = CopyNotificationIdDataset(DatasetServiceGrpcKt.DatasetServiceCoroutineStub(channel))::forward
    }
    write(rec)
}
