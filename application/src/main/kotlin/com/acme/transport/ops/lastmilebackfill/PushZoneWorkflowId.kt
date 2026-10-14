package com.acme.transport.ops.lastmilebackfill

import com.acme.dispatch.v1.ZoneWorkflowServiceGrpcKt
import com.acme.dispatch.v1.advanceZoneWorkflowRequest
import com.acme.driverapp.v1.DriverInboxApprovalServiceGrpcKt
import com.acme.driverapp.v1.SubmitDriverInboxRequestKt
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.submitDriverInboxRequest
import com.acme.routing.v1.CheckRouteEligibilityRequestKt
import com.acme.routing.v1.RouteValidationServiceGrpcKt
import com.acme.routing.v1.checkRouteEligibilityRequest
import com.acme.routing.v1.route
import com.acme.travel.v1.TripCalculationServiceGrpcKt
import com.acme.travel.v1.estimateTripRequest
import com.acme.travel.v1.trip
import io.grpc.ManagedChannel

/** Reads acme.dispatch.v1.ZoneWorkflowService/AdvanceZoneWorkflow and hands what it keeps to the sink of its kind. */
suspend fun pushZoneWorkflowId(channel: ManagedChannel) {
    val origin = ZoneWorkflowServiceGrpcKt.ZoneWorkflowServiceCoroutineStub(channel)
    val found = origin.advanceZoneWorkflow(advanceZoneWorkflowRequest { this.zoneId = "current-push-zone-workflow-id" })
    val entry = PushZoneWorkflowIdRecord(
        parentId = found.zone.parentId,
        referenceId = found.outcome.referenceId,
        message = found.outcome.message,
        status = found.result.status,
    )
    val sinks = listOf<PushZoneWorkflowIdSink>(
        PushZoneWorkflowIdTripCalculation(TripCalculationServiceGrpcKt.TripCalculationServiceCoroutineStub(channel)),
        PushZoneWorkflowIdDriverInboxApproval(DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub(channel)),
        PushZoneWorkflowIdRouteValidation(RouteValidationServiceGrpcKt.RouteValidationServiceCoroutineStub(channel)),
    )
    sinks[0].relay(entry)
}

/** Where pushZoneWorkflowId hands each record. */
interface PushZoneWorkflowIdSink {
    suspend fun relay(rec: PushZoneWorkflowIdRecord)
}

/** What pushZoneWorkflowId keeps of each AdvanceZoneWorkflow response. */
data class PushZoneWorkflowIdRecord(val parentId: String, val referenceId: String, val message: String, val status: String)

/** Writes records to acme.travel.v1.TripCalculationService. */
class PushZoneWorkflowIdTripCalculation(private val stub: TripCalculationServiceGrpcKt.TripCalculationServiceCoroutineStub) : PushZoneWorkflowIdSink {
    override suspend fun relay(rec: PushZoneWorkflowIdRecord) {
        stub.estimateTrip(estimateTripRequest { this.trip = trip { this.number = rec.status } })
    }
}

/** Writes records to acme.driverapp.v1.DriverInboxApprovalService. */
class PushZoneWorkflowIdDriverInboxApproval(private val stub: DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub) : PushZoneWorkflowIdSink {
    override suspend fun relay(rec: PushZoneWorkflowIdRecord) {
        val request = submitDriverInboxRequest {
            this.driverInbox = driverInbox { this.driverInboxId = rec.parentId }
            this.details = SubmitDriverInboxRequestKt.details { this.comment = rec.status; this.approverEmail = rec.referenceId }
        }
        stub.submitDriverInbox(request)
    }
}

/** Passes records to acme.routing.v1.RouteValidationService. */
class PushZoneWorkflowIdRouteValidation(private val stub: RouteValidationServiceGrpcKt.RouteValidationServiceCoroutineStub) : PushZoneWorkflowIdSink {
    override suspend fun relay(rec: PushZoneWorkflowIdRecord) {
        val request = checkRouteEligibilityRequest {
            this.details = CheckRouteEligibilityRequestKt.details { this.locale = rec.status }
            this.route = route { this.number = rec.message; this.notes = rec.referenceId }
        }
        stub.checkRouteEligibility(request)
    }
}
