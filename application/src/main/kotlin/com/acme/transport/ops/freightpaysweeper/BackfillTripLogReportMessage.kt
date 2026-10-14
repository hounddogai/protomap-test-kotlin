package com.acme.transport.ops.freightpaysweeper

import com.acme.datasets.v1.DatasetServiceGrpcKt
import com.acme.datasets.v1.cloneDatasetRequest
import com.acme.deliveries.v1.CountDeliveriesRequestKt
import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.countDeliveriesRequest
import com.acme.sponsorships.v1.BrowseSponsorsRequestKt
import com.acme.sponsorships.v1.SponsorQueryServiceGrpcKt
import com.acme.sponsorships.v1.browseSponsorsRequest
import com.acme.telematics.v1.TripLogReportingServiceGrpcKt
import com.acme.telematics.v1.fetchTripLogReportRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TripLogReportingService/FetchTripLogReport and hands what it keeps to the sink of its kind. */
suspend fun backfillTripLogReportMessage(channel: ManagedChannel) {
    val reader = TripLogReportingServiceGrpcKt.TripLogReportingServiceCoroutineStub(channel)
    val current = reader.fetchTripLogReport(fetchTripLogReportRequest { this.tripLogId = "current-backfill-trip-log-report-message" })
    val rec = BackfillTripLogReportMessageRecord(
        message = current.result.message,
        tripLogId = current.tripLog.tripLogId,
    )
    actOnBackfillTripLogReportMessage("delivery-query", rec, channel)
}

/** Where backfillTripLogReportMessage hands each record. */
interface BackfillTripLogReportMessageWriter {
    suspend fun write(rec: BackfillTripLogReportMessageRecord)
}

/** What backfillTripLogReportMessage keeps of each FetchTripLogReport response. */
data class BackfillTripLogReportMessageRecord(val message: String, val tripLogId: String)

/** Hands records to acme.datasets.v1.DatasetService. */
class BackfillTripLogReportMessageDataset(private val stub: DatasetServiceGrpcKt.DatasetServiceCoroutineStub) : BackfillTripLogReportMessageWriter {
    override suspend fun write(rec: BackfillTripLogReportMessageRecord) {
        stub.cloneDataset(cloneDatasetRequest { this.requestId = rec.message })
    }
}

/** Writes records to acme.deliveries.v1.DeliveryQueryService. */
class BackfillTripLogReportMessageDeliveryQuery(private val stub: DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub) : BackfillTripLogReportMessageWriter {
    override suspend fun write(rec: BackfillTripLogReportMessageRecord) {
        val request = countDeliveriesRequest {
            this.filter = CountDeliveriesRequestKt.filter { this.region = rec.tripLogId }
        }
        RpcBoundary.run("acme.deliveries.v1.DeliveryQueryService/CountDeliveries") { stub.countDeliveries(request) }
    }
}

/** Passes records to acme.sponsorships.v1.SponsorQueryService. */
class BackfillTripLogReportMessageSponsorQuery(private val stub: SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub) : BackfillTripLogReportMessageWriter {
    override suspend fun write(rec: BackfillTripLogReportMessageRecord) {
        val request = browseSponsorsRequest {
            this.orderBy = rec.message
            this.filter = BrowseSponsorsRequestKt.filter { this.ownerId = rec.tripLogId }
        }
        stub.browseSponsors(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnBackfillTripLogReportMessage(kind: String, rec: BackfillTripLogReportMessageRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "dataset" to { BackfillTripLogReportMessageDataset(DatasetServiceGrpcKt.DatasetServiceCoroutineStub(channel)).write(rec) },
        "delivery-query" to { BackfillTripLogReportMessageDeliveryQuery(DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub(channel)).write(rec) },
        "sponsor-query" to { BackfillTripLogReportMessageSponsorQuery(SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub(channel)).write(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
