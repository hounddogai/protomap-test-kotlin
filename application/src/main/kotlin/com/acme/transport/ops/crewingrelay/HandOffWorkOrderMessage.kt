package com.acme.transport.ops.crewingrelay

import com.acme.common.v1.pageRequest
import com.acme.dispatch.v1.BrowseDispatchOrdersRequestKt
import com.acme.dispatch.v1.DispatchOrderQueryServiceGrpcKt
import com.acme.dispatch.v1.browseDispatchOrdersRequest
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.previewWorkOrderRequest
import com.acme.seating.v1.SeatMapCalculationServiceGrpcKt
import com.acme.seating.v1.getSeatMapRatesRequest
import com.acme.sponsorships.v1.SimulateSponsorRequestKt
import com.acme.sponsorships.v1.SponsorCalculationServiceGrpcKt
import com.acme.sponsorships.v1.SponsorServiceGrpcKt
import com.acme.sponsorships.v1.cloneSponsorRequest
import com.acme.sponsorships.v1.simulateSponsorRequest
import com.acme.sponsorships.v1.sponsor
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.maintenance.v1.WorkOrderValidationService/PreviewWorkOrder and hands what it keeps to the sink of its kind. */
suspend fun handOffWorkOrderMessage(channel: ManagedChannel) {
    val reader = WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub(channel)
    val current = RpcBoundary.read { reader.previewWorkOrder(previewWorkOrderRequest { this.reason = "default-hand-off-work-order-message" }) }
    val rec = HandOffWorkOrderMessageRecord(
        message = current.result.message,
        correlationId = current.result.correlationId,
        status = current.result.status,
    )
    deliverHandOffWorkOrderMessage("sponsor", rec, channel)
}

/** Where handOffWorkOrderMessage hands each record. */
interface HandOffWorkOrderMessageSink {
    suspend fun write(rec: HandOffWorkOrderMessageRecord)
}

/** What handOffWorkOrderMessage keeps of each PreviewWorkOrder response. */
data class HandOffWorkOrderMessageRecord(val message: String, val correlationId: String, val status: String)

/** Hands records to acme.sponsorships.v1.SponsorService. */
class HandOffWorkOrderMessageSponsor(private val stub: SponsorServiceGrpcKt.SponsorServiceCoroutineStub) : HandOffWorkOrderMessageSink {
    override suspend fun write(rec: HandOffWorkOrderMessageRecord) {
        val request = cloneSponsorRequest {
            this.requestId = rec.status
            this.sponsor = sponsor { this.sponsorId = rec.correlationId }
        }
        RpcBoundary.run("acme.sponsorships.v1.SponsorService/CloneSponsor") { stub.cloneSponsor(request) }
    }
}

/** Hands records to acme.seating.v1.SeatMapCalculationService. */
class HandOffWorkOrderMessageSeatMapCalculation(private val stub: SeatMapCalculationServiceGrpcKt.SeatMapCalculationServiceCoroutineStub) : HandOffWorkOrderMessageSink {
    override suspend fun write(rec: HandOffWorkOrderMessageRecord) {
        RpcBoundary.run("acme.seating.v1.SeatMapCalculationService/GetSeatMapRates") { stub.getSeatMapRates(getSeatMapRatesRequest { this.seatMapId = rec.correlationId }) }
    }
}

/** Writes records to acme.sponsorships.v1.SponsorCalculationService. */
class HandOffWorkOrderMessageSponsorCalculation(private val stub: SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub) : HandOffWorkOrderMessageSink {
    override suspend fun write(rec: HandOffWorkOrderMessageRecord) {
        val request = simulateSponsorRequest {
            this.details = SimulateSponsorRequestKt.details { this.currencyCode = rec.message }
            this.reason = rec.status
            this.sponsorId = rec.correlationId
        }
        RpcBoundary.run("acme.sponsorships.v1.SponsorCalculationService/SimulateSponsor") { stub.simulateSponsor(request) }
    }
}

/** Hands records to acme.dispatch.v1.DispatchOrderQueryService. */
class HandOffWorkOrderMessageDispatchOrderQuery(private val stub: DispatchOrderQueryServiceGrpcKt.DispatchOrderQueryServiceCoroutineStub) : HandOffWorkOrderMessageSink {
    override suspend fun write(rec: HandOffWorkOrderMessageRecord) {
        val request = browseDispatchOrdersRequest {
            this.filter = BrowseDispatchOrdersRequestKt.filter { this.region = rec.status; this.ownerId = rec.correlationId }
            this.page = pageRequest { this.pageToken = rec.message }
        }
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderQueryService/BrowseDispatchOrders") { stub.browseDispatchOrders(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class HandOffWorkOrderMessageLogOnly : HandOffWorkOrderMessageSink {
    override suspend fun write(rec: HandOffWorkOrderMessageRecord) {
        LoggerFactory.getLogger("HandOffWorkOrderMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverHandOffWorkOrderMessage(kind: String, rec: HandOffWorkOrderMessageRecord, channel: ManagedChannel) {
    var sink: HandOffWorkOrderMessageSink = HandOffWorkOrderMessageLogOnly()
    when (kind) {
        "sponsor" -> sink = HandOffWorkOrderMessageSponsor(SponsorServiceGrpcKt.SponsorServiceCoroutineStub(channel))
        "seat-map-calculation" -> sink = HandOffWorkOrderMessageSeatMapCalculation(SeatMapCalculationServiceGrpcKt.SeatMapCalculationServiceCoroutineStub(channel))
        "sponsor-calculation" -> sink = HandOffWorkOrderMessageSponsorCalculation(SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub(channel))
        "dispatch-order-query" -> sink = HandOffWorkOrderMessageDispatchOrderQuery(DispatchOrderQueryServiceGrpcKt.DispatchOrderQueryServiceCoroutineStub(channel))
    }
    sink.write(rec)
}
