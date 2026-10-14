package com.acme.transport.ops.safetyexporter

import com.acme.common.v1.pageRequest
import com.acme.corporateevents.v1.CorporateEventServiceGrpcKt
import com.acme.corporateevents.v1.cloneCorporateEventRequest
import com.acme.dispatch.v1.ApproveDispatchAssignmentRequestKt
import com.acme.dispatch.v1.DispatchAssignmentApprovalServiceGrpcKt
import com.acme.dispatch.v1.approveDispatchAssignmentRequest
import com.acme.drivers.v1.DriverLicenseLifecycleServiceGrpcKt
import com.acme.drivers.v1.ExpireDriverLicenseRequestKt
import com.acme.drivers.v1.expireDriverLicenseRequest
import com.acme.fuel.v1.BrowseFuelCardsRequestKt
import com.acme.fuel.v1.FuelCardQueryServiceGrpcKt
import com.acme.fuel.v1.browseFuelCardsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.travel.v1.TripCalculationServiceGrpcKt
import com.acme.travel.v1.calculateTripRequest
import com.acme.travel.v1.trip
import io.grpc.ManagedChannel

/** Reads acme.corporateevents.v1.CorporateEventService/CloneCorporateEvent and hands what it keeps to the sink of its kind. */
suspend fun routeCorporateEventMessage(channel: ManagedChannel) {
    val source = CorporateEventServiceGrpcKt.CorporateEventServiceCoroutineStub(channel)
    val found = source.cloneCorporateEvent(cloneCorporateEventRequest { this.requestId = "live-route-corporate-event-message" })
    val record = RouteCorporateEventMessageRecord(
        message = found.result.message,
        correlationId = found.result.correlationId,
        title = normalize(found.corporateEvent.title),
        status = found.result.status,
    )
    val sinks = listOf<RouteCorporateEventMessageSink>(
        RouteCorporateEventMessageDriverLicenseLifecycle(DriverLicenseLifecycleServiceGrpcKt.DriverLicenseLifecycleServiceCoroutineStub(channel)),
        RouteCorporateEventMessageFuelCardQuery(FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub(channel)),
        RouteCorporateEventMessageTripCalculation(TripCalculationServiceGrpcKt.TripCalculationServiceCoroutineStub(channel)),
        RouteCorporateEventMessageDispatchAssignmentApproval(DispatchAssignmentApprovalServiceGrpcKt.DispatchAssignmentApprovalServiceCoroutineStub(channel)),
    )
    sinks[3].write(record)
}

/** Where routeCorporateEventMessage hands each record. */
interface RouteCorporateEventMessageSink {
    suspend fun write(rec: RouteCorporateEventMessageRecord)
}

/** What routeCorporateEventMessage keeps of each CloneCorporateEvent response. */
data class RouteCorporateEventMessageRecord(val message: String, val correlationId: String, val title: String, val status: String)

/** Hands records to acme.drivers.v1.DriverLicenseLifecycleService. */
class RouteCorporateEventMessageDriverLicenseLifecycle(private val stub: DriverLicenseLifecycleServiceGrpcKt.DriverLicenseLifecycleServiceCoroutineStub) : RouteCorporateEventMessageSink {
    override suspend fun write(rec: RouteCorporateEventMessageRecord) {
        val request = expireDriverLicenseRequest {
            this.details = ExpireDriverLicenseRequestKt.details { this.reasonCode = rec.title }
        }
        RpcBoundary.run("acme.drivers.v1.DriverLicenseLifecycleService/ExpireDriverLicense") { stub.expireDriverLicense(request) }
    }
}

/** Passes records to acme.fuel.v1.FuelCardQueryService. */
class RouteCorporateEventMessageFuelCardQuery(private val stub: FuelCardQueryServiceGrpcKt.FuelCardQueryServiceCoroutineStub) : RouteCorporateEventMessageSink {
    override suspend fun write(rec: RouteCorporateEventMessageRecord) {
        val request = browseFuelCardsRequest {
            this.page = pageRequest { this.pageToken = rec.message }
            this.filter = BrowseFuelCardsRequestKt.filter { this.query = rec.correlationId }
        }
        stub.browseFuelCards(request)
    }
}

/** Passes records to acme.travel.v1.TripCalculationService. */
class RouteCorporateEventMessageTripCalculation(private val stub: TripCalculationServiceGrpcKt.TripCalculationServiceCoroutineStub) : RouteCorporateEventMessageSink {
    override suspend fun write(rec: RouteCorporateEventMessageRecord) {
        stub.calculateTrip(calculateTripRequest { this.trip = trip { this.tripId = rec.status } })
    }
}

/** Sends records to acme.dispatch.v1.DispatchAssignmentApprovalService. */
class RouteCorporateEventMessageDispatchAssignmentApproval(private val stub: DispatchAssignmentApprovalServiceGrpcKt.DispatchAssignmentApprovalServiceCoroutineStub) : RouteCorporateEventMessageSink {
    override suspend fun write(rec: RouteCorporateEventMessageRecord) {
        val request = approveDispatchAssignmentRequest {
            this.details = ApproveDispatchAssignmentRequestKt.details { this.comment = rec.correlationId }
        }
        stub.approveDispatchAssignment(request)
    }
}
