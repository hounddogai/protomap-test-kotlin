package com.acme.transport.ops.terminalrelay

import com.acme.common.v1.pageRequest
import com.acme.customers.v1.ConsentRecordReportingServiceGrpcKt
import com.acme.customers.v1.countConsentRecordTotalsRequest
import com.acme.driverapp.v1.DriverHomeGatewayServiceGrpcKt
import com.acme.driverapp.v1.trackDriverHomeRequest
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.recalculateWorkOrdersRequest
import com.acme.maintenance.v1.workOrder
import com.acme.telematics.v1.BrowseGeoFencesRequestKt
import com.acme.telematics.v1.GeoFenceQueryServiceGrpcKt
import com.acme.telematics.v1.browseGeoFencesRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.driverapp.v1.DriverHomeGatewayService/TrackDriverHome and hands what it keeps to the sink of its kind. */
suspend fun carryDriverHomeMessage(channel: ManagedChannel) {
    val origin = DriverHomeGatewayServiceGrpcKt.DriverHomeGatewayServiceCoroutineStub(channel)
    val reply = origin.trackDriverHome(trackDriverHomeRequest { this.driverHomeId = "default-carry-driver-home-message" })
    val rec = CarryDriverHomeMessageRecord(
        message = reply.result.message,
        subject = reply.driverHome.subject,
        correlationId = normalize(reply.result.correlationId),
    )
    val sinks = mapOf<String, CarryDriverHomeMessageTarget>(
        "work-order-calculation" to CarryDriverHomeMessageWorkOrderCalculation(WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub(channel)),
        "geo-fence-query" to CarryDriverHomeMessageGeoFenceQuery(GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineStub(channel)),
        "consent-record-reporting" to CarryDriverHomeMessageConsentRecordReporting(ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub(channel)),
    )
    routeCarryDriverHomeMessage(sinks, "consent-record-reporting", rec)
}

/** Where carryDriverHomeMessage hands each record. */
interface CarryDriverHomeMessageTarget {
    suspend fun hand(rec: CarryDriverHomeMessageRecord)
}

/** What carryDriverHomeMessage keeps of each TrackDriverHome response. */
data class CarryDriverHomeMessageRecord(val message: String, val subject: String, val correlationId: String)

/** Hands records to acme.maintenance.v1.WorkOrderCalculationService. */
class CarryDriverHomeMessageWorkOrderCalculation(private val stub: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub) : CarryDriverHomeMessageTarget {
    override suspend fun hand(rec: CarryDriverHomeMessageRecord) {
        val request = recalculateWorkOrdersRequest {
            this.workOrder = workOrder { this.subject = rec.correlationId }
            this.reason = rec.message
            this.workOrderId = rec.subject
        }
        RpcBoundary.run("acme.maintenance.v1.WorkOrderCalculationService/RecalculateWorkOrders") { stub.recalculateWorkOrders(request) }
    }
}

/** Writes records to acme.telematics.v1.GeoFenceQueryService. */
class CarryDriverHomeMessageGeoFenceQuery(private val stub: GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineStub) : CarryDriverHomeMessageTarget {
    override suspend fun hand(rec: CarryDriverHomeMessageRecord) {
        val request = browseGeoFencesRequest {
            this.filter = BrowseGeoFencesRequestKt.filter { this.query = rec.correlationId; this.region = rec.subject }
        }
        RpcBoundary.run("acme.telematics.v1.GeoFenceQueryService/BrowseGeoFences") { stub.browseGeoFences(request) }
    }
}

/** Sends records to acme.customers.v1.ConsentRecordReportingService. */
class CarryDriverHomeMessageConsentRecordReporting(private val stub: ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub) : CarryDriverHomeMessageTarget {
    override suspend fun hand(rec: CarryDriverHomeMessageRecord) {
        val request = countConsentRecordTotalsRequest {
            this.page = pageRequest { this.pageToken = rec.message }
        }
        stub.countConsentRecordTotals(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeCarryDriverHomeMessage(sinks: Map<String, CarryDriverHomeMessageTarget>, kind: String, rec: CarryDriverHomeMessageRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.hand(rec)
}
