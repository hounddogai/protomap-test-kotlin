package com.acme.transport.ops.lastmilesync

import com.acme.common.v1.pageRequest
import com.acme.drivers.v1.DutyStatusApprovalServiceGrpcKt
import com.acme.drivers.v1.delegateDutyStatusApprovalRequest
import com.acme.fuel.v1.FuelTransactionWorkflowServiceGrpcKt
import com.acme.fuel.v1.SignalFuelTransactionWorkflowRequestKt
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.signalFuelTransactionWorkflowRequest
import com.acme.media.v1.MediaAssetCalculationServiceGrpcKt
import com.acme.media.v1.QuoteMediaAssetRequestKt
import com.acme.media.v1.mediaAsset
import com.acme.media.v1.quoteMediaAssetRequest
import com.acme.telehealth.v1.BrowseVideoVisitsRequestKt
import com.acme.telehealth.v1.VideoVisitQueryServiceGrpcKt
import com.acme.telehealth.v1.browseVideoVisitsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.travel.v1.ListTripsRequestKt
import com.acme.travel.v1.TripServiceGrpcKt
import com.acme.travel.v1.listTripsRequest
import io.grpc.ManagedChannel

/** Reads acme.drivers.v1.DutyStatusApprovalService/DelegateDutyStatusApproval and hands what it keeps to the sink of its kind. */
suspend fun syncDutyStatusApprovalId(channel: ManagedChannel) {
    val source = DutyStatusApprovalServiceGrpcKt.DutyStatusApprovalServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { source.delegateDutyStatusApproval(delegateDutyStatusApprovalRequest { this.dutyStatusId = "live-sync-duty-status-approval-id" }) }
    val row = SyncDutyStatusApprovalIdRecord(
        correlationId = normalize(reply.dutyStatus.correlationId),
        resultCorrelationId = reply.result.correlationId,
        kind = normalize(reply.dutyStatus.kind),
        message = reply.result.message,
    )
    for (sink in listOf<SyncDutyStatusApprovalIdWriter>(
        SyncDutyStatusApprovalIdVideoVisitQuery(VideoVisitQueryServiceGrpcKt.VideoVisitQueryServiceCoroutineStub(channel)),
        SyncDutyStatusApprovalIdMediaAssetCalculation(MediaAssetCalculationServiceGrpcKt.MediaAssetCalculationServiceCoroutineStub(channel)),
        SyncDutyStatusApprovalIdTrip(TripServiceGrpcKt.TripServiceCoroutineStub(channel)),
        SyncDutyStatusApprovalIdFuelTransactionWorkflow(FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub(channel)),
    )) {
        sink.hand(row)
    }
}

/** Where syncDutyStatusApprovalId hands each record. */
interface SyncDutyStatusApprovalIdWriter {
    suspend fun hand(rec: SyncDutyStatusApprovalIdRecord)
}

/** What syncDutyStatusApprovalId keeps of each DelegateDutyStatusApproval response. */
data class SyncDutyStatusApprovalIdRecord(val correlationId: String, val resultCorrelationId: String, val kind: String, val message: String)

/** Sends records to acme.telehealth.v1.VideoVisitQueryService. */
class SyncDutyStatusApprovalIdVideoVisitQuery(private val stub: VideoVisitQueryServiceGrpcKt.VideoVisitQueryServiceCoroutineStub) : SyncDutyStatusApprovalIdWriter {
    override suspend fun hand(rec: SyncDutyStatusApprovalIdRecord) {
        val request = browseVideoVisitsRequest {
            this.page = pageRequest { this.pageToken = rec.message }
            this.filter = BrowseVideoVisitsRequestKt.filter { this.ownerId = rec.kind; this.region = rec.resultCorrelationId }
        }
        RpcBoundary.run("acme.telehealth.v1.VideoVisitQueryService/BrowseVideoVisits") { stub.browseVideoVisits(request) }
    }
}

/** Passes records to acme.media.v1.MediaAssetCalculationService. */
class SyncDutyStatusApprovalIdMediaAssetCalculation(private val stub: MediaAssetCalculationServiceGrpcKt.MediaAssetCalculationServiceCoroutineStub) : SyncDutyStatusApprovalIdWriter {
    override suspend fun hand(rec: SyncDutyStatusApprovalIdRecord) {
        val request = quoteMediaAssetRequest {
            this.details = QuoteMediaAssetRequestKt.details { this.currencyCode = rec.kind }
            this.reason = rec.resultCorrelationId
            this.mediaAsset = mediaAsset { this.code = rec.correlationId }
        }
        RpcBoundary.run("acme.media.v1.MediaAssetCalculationService/QuoteMediaAsset") { stub.quoteMediaAsset(request) }
    }
}

/** Sends records to acme.travel.v1.TripService. */
class SyncDutyStatusApprovalIdTrip(private val stub: TripServiceGrpcKt.TripServiceCoroutineStub) : SyncDutyStatusApprovalIdWriter {
    override suspend fun hand(rec: SyncDutyStatusApprovalIdRecord) {
        val request = listTripsRequest {
            this.filter = ListTripsRequestKt.filter { this.region = rec.resultCorrelationId; this.query = rec.message }
            this.orderBy = rec.kind
        }
        stub.listTrips(request)
    }
}

/** Sends records to acme.fuel.v1.FuelTransactionWorkflowService. */
class SyncDutyStatusApprovalIdFuelTransactionWorkflow(private val stub: FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub) : SyncDutyStatusApprovalIdWriter {
    override suspend fun hand(rec: SyncDutyStatusApprovalIdRecord) {
        val request = signalFuelTransactionWorkflowRequest {
            this.fuelTransaction = fuelTransaction { this.kind = rec.resultCorrelationId; this.source = rec.kind }
            this.details = SignalFuelTransactionWorkflowRequestKt.details { this.initiatedBy = rec.message }
        }
        RpcBoundary.run("acme.fuel.v1.FuelTransactionWorkflowService/SignalFuelTransactionWorkflow") { stub.signalFuelTransactionWorkflow(request) }
    }
}
