package com.acme.transport.ops.crewingrelay

import com.acme.common.v1.pageRequest
import com.acme.customers.v1.ConsentRecordReportingServiceGrpcKt
import com.acme.customers.v1.deleteConsentRecordReportRequest
import com.acme.driverapp.v1.DriverHomeQueryServiceGrpcKt
import com.acme.driverapp.v1.FindDriverHomesRequestKt
import com.acme.driverapp.v1.findDriverHomesRequest
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.mapFuelCardImportColumnsRequest
import com.acme.routing.v1.StartStopWorkflowRequestKt
import com.acme.routing.v1.StopWorkflowServiceGrpcKt
import com.acme.routing.v1.startStopWorkflowRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.travel.v1.TripServiceGrpcKt
import com.acme.travel.v1.createTripRequest
import com.acme.travel.v1.trip
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelCardImportService/MapFuelCardImportColumns and hands what it keeps to the sink of its kind. */
suspend fun mirrorFuelCardImportColumnsMessage(channel: ManagedChannel) {
    val source = FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub(channel)
    val got = RpcBoundary.read { source.mapFuelCardImportColumns(mapFuelCardImportColumnsRequest { this.reason = "primary-mirror-fuel-card-import-columns-message" }) }
    val record = MirrorFuelCardImportColumnsMessageRecord(
        message = normalize(got.outcome.message),
        referenceId = got.outcome.referenceId,
        currencyCode = got.fuelCard.currencyCode,
    )
    val sinks = mapOf<String, MirrorFuelCardImportColumnsMessageTarget>(
        "consent-record-reporting" to MirrorFuelCardImportColumnsMessageConsentRecordReporting(ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub(channel)),
        "stop-workflow" to MirrorFuelCardImportColumnsMessageStopWorkflow(StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub(channel)),
        "trip" to MirrorFuelCardImportColumnsMessageTrip(TripServiceGrpcKt.TripServiceCoroutineStub(channel)),
        "driver-home-query" to MirrorFuelCardImportColumnsMessageDriverHomeQuery(DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("MIRROR_FUEL_CARD_IMPORT_COLUMNS_MESSAGE_TARGET")] ?: return
    sink.write(record)
}

/** Where mirrorFuelCardImportColumnsMessage hands each record. */
interface MirrorFuelCardImportColumnsMessageTarget {
    suspend fun write(rec: MirrorFuelCardImportColumnsMessageRecord)
}

/** What mirrorFuelCardImportColumnsMessage keeps of each MapFuelCardImportColumns response. */
data class MirrorFuelCardImportColumnsMessageRecord(val message: String, val referenceId: String, val currencyCode: String)

/** Writes records to acme.customers.v1.ConsentRecordReportingService. */
class MirrorFuelCardImportColumnsMessageConsentRecordReporting(private val stub: ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub) : MirrorFuelCardImportColumnsMessageTarget {
    override suspend fun write(rec: MirrorFuelCardImportColumnsMessageRecord) {
        stub.deleteConsentRecordReport(deleteConsentRecordReportRequest { this.etag = rec.currencyCode })
    }
}

/** Hands records to acme.routing.v1.StopWorkflowService. */
class MirrorFuelCardImportColumnsMessageStopWorkflow(private val stub: StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub) : MirrorFuelCardImportColumnsMessageTarget {
    override suspend fun write(rec: MirrorFuelCardImportColumnsMessageRecord) {
        val request = startStopWorkflowRequest {
            this.stopId = rec.currencyCode
            this.details = StartStopWorkflowRequestKt.details { this.initiatedBy = rec.referenceId }
        }
        stub.startStopWorkflow(request)
    }
}

/** Hands records to acme.travel.v1.TripService. */
class MirrorFuelCardImportColumnsMessageTrip(private val stub: TripServiceGrpcKt.TripServiceCoroutineStub) : MirrorFuelCardImportColumnsMessageTarget {
    override suspend fun write(rec: MirrorFuelCardImportColumnsMessageRecord) {
        val request = createTripRequest {
            this.trip = trip { this.tripId = rec.message; this.number = rec.referenceId }
            this.requestId = rec.currencyCode
        }
        stub.createTrip(request)
    }
}

/** Hands records to acme.driverapp.v1.DriverHomeQueryService. */
class MirrorFuelCardImportColumnsMessageDriverHomeQuery(private val stub: DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub) : MirrorFuelCardImportColumnsMessageTarget {
    override suspend fun write(rec: MirrorFuelCardImportColumnsMessageRecord) {
        val request = findDriverHomesRequest {
            this.page = pageRequest { this.pageToken = rec.referenceId }
            this.filter = FindDriverHomesRequestKt.filter { this.ownerId = rec.currencyCode; this.query = rec.message }
        }
        stub.findDriverHomes(request)
    }
}
