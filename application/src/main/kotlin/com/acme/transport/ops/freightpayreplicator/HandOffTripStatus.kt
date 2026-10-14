package com.acme.transport.ops.freightpayreplicator

import com.acme.customers.v1.ConsentRecordWorkflowServiceGrpcKt
import com.acme.customers.v1.ListConsentRecordWorkflowsRequestKt
import com.acme.customers.v1.listConsentRecordWorkflowsRequest
import com.acme.seating.v1.SeatMapServiceGrpcKt
import com.acme.seating.v1.createSeatMapRequest
import com.acme.seating.v1.seatMap
import com.acme.sponsorships.v1.SponsorServiceGrpcKt
import com.acme.sponsorships.v1.sponsor
import com.acme.sponsorships.v1.updateSponsorRequest
import com.acme.travel.v1.TripServiceGrpcKt
import com.acme.travel.v1.createTripRequest
import io.grpc.ManagedChannel

/** Reads acme.travel.v1.TripService/CreateTrip and hands what it keeps to the sink of its kind. */
suspend fun handOffTripStatus(channel: ManagedChannel) {
    val upstream = TripServiceGrpcKt.TripServiceCoroutineStub(channel)
    val reply = upstream.createTrip(createTripRequest { this.requestId = "live-hand-off-trip-status" })
    val rec = HandOffTripStatusRecord(
        status = reply.result.status,
        correlationId = reply.result.correlationId,
        message = reply.result.message,
    )
    val sinks = mapOf<String, HandOffTripStatusSink>(
        "seat-map" to HandOffTripStatusSeatMap(SeatMapServiceGrpcKt.SeatMapServiceCoroutineStub(channel)),
        "consent-record-workflow" to HandOffTripStatusConsentRecordWorkflow(ConsentRecordWorkflowServiceGrpcKt.ConsentRecordWorkflowServiceCoroutineStub(channel)),
        "sponsor" to HandOffTripStatusSponsor(SponsorServiceGrpcKt.SponsorServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "consent-record-workflow") continue
        sinks[kind]?.transmit(rec)
    }
}

/** Where handOffTripStatus hands each record. */
interface HandOffTripStatusSink {
    suspend fun transmit(rec: HandOffTripStatusRecord)
}

/** What handOffTripStatus keeps of each CreateTrip response. */
data class HandOffTripStatusRecord(val status: String, val correlationId: String, val message: String)

/** Sends records to acme.seating.v1.SeatMapService. */
class HandOffTripStatusSeatMap(private val stub: SeatMapServiceGrpcKt.SeatMapServiceCoroutineStub) : HandOffTripStatusSink {
    override suspend fun transmit(rec: HandOffTripStatusRecord) {
        val request = createSeatMapRequest {
            this.requestId = rec.message
            this.seatMap = seatMap { this.parentId = rec.correlationId }
        }
        stub.createSeatMap(request)
    }
}

/** Writes records to acme.customers.v1.ConsentRecordWorkflowService. */
class HandOffTripStatusConsentRecordWorkflow(private val stub: ConsentRecordWorkflowServiceGrpcKt.ConsentRecordWorkflowServiceCoroutineStub) : HandOffTripStatusSink {
    override suspend fun transmit(rec: HandOffTripStatusRecord) {
        val request = listConsentRecordWorkflowsRequest {
            this.filter = ListConsentRecordWorkflowsRequestKt.filter { this.query = rec.message }
        }
        stub.listConsentRecordWorkflows(request)
    }
}

/** Writes records to acme.sponsorships.v1.SponsorService. */
class HandOffTripStatusSponsor(private val stub: SponsorServiceGrpcKt.SponsorServiceCoroutineStub) : HandOffTripStatusSink {
    override suspend fun transmit(rec: HandOffTripStatusRecord) {
        stub.updateSponsor(updateSponsorRequest { this.sponsor = sponsor { this.sponsorId = rec.correlationId } })
    }
}
