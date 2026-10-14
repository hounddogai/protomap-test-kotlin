package com.acme.transport.ops.fleetopsreconciler

import com.acme.driverapp.v1.DriverHomeWorkflowServiceGrpcKt
import com.acme.driverapp.v1.StartDriverHomeWorkflowRequestKt
import com.acme.driverapp.v1.startDriverHomeWorkflowRequest
import com.acme.drivers.v1.ApproveDriverLicenseRequestKt
import com.acme.drivers.v1.DriverLicenseApprovalServiceGrpcKt
import com.acme.drivers.v1.approveDriverLicenseRequest
import com.acme.shows.v1.ShowServiceGrpcKt
import com.acme.shows.v1.deleteShowRequest
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.explainTripLogDecisionRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.shows.v1.ShowService/DeleteShow and hands what it keeps to the sink of its kind. */
suspend fun relayShowMessage(channel: ManagedChannel) {
    val origin = ShowServiceGrpcKt.ShowServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { origin.deleteShow(deleteShowRequest { this.reason = "current-relay-show-message" }) }
    val row = RelayShowMessageRecord(
        message = reply.result.message,
        correlationId = normalize(reply.result.correlationId),
        status = reply.result.status,
    )
    val registry = mutableMapOf<String, RelayShowMessageWriter>()
    for (registration in listOf(
        RelayShowMessageRegistration("driver-license-approval", RelayShowMessageDriverLicenseApproval(DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub(channel))),
        RelayShowMessageRegistration("driver-home-workflow", RelayShowMessageDriverHomeWorkflow(DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub(channel))),
        RelayShowMessageRegistration("trip-log-validation", RelayShowMessageTripLogValidation(TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["trip-log-validation"] ?: return
    sink.deliver(row)
}

/** Where relayShowMessage hands each record. */
interface RelayShowMessageWriter {
    suspend fun deliver(rec: RelayShowMessageRecord)
}

/** What relayShowMessage keeps of each DeleteShow response. */
data class RelayShowMessageRecord(val message: String, val correlationId: String, val status: String)

/** Hands records to acme.drivers.v1.DriverLicenseApprovalService. */
class RelayShowMessageDriverLicenseApproval(private val stub: DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub) : RelayShowMessageWriter {
    override suspend fun deliver(rec: RelayShowMessageRecord) {
        val request = approveDriverLicenseRequest {
            this.details = ApproveDriverLicenseRequestKt.details { this.approverEmail = rec.correlationId }
        }
        RpcBoundary.run("acme.drivers.v1.DriverLicenseApprovalService/ApproveDriverLicense") { stub.approveDriverLicense(request) }
    }
}

/** Sends records to acme.driverapp.v1.DriverHomeWorkflowService. */
class RelayShowMessageDriverHomeWorkflow(private val stub: DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub) : RelayShowMessageWriter {
    override suspend fun deliver(rec: RelayShowMessageRecord) {
        val request = startDriverHomeWorkflowRequest {
            this.driverHomeId = rec.message
            this.details = StartDriverHomeWorkflowRequestKt.details { this.initiatedBy = rec.correlationId }
        }
        stub.startDriverHomeWorkflow(request)
    }
}

/** Passes records to acme.telematics.v1.TripLogValidationService. */
class RelayShowMessageTripLogValidation(private val stub: TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub) : RelayShowMessageWriter {
    override suspend fun deliver(rec: RelayShowMessageRecord) {
        val request = explainTripLogDecisionRequest {
            this.reason = rec.message
            this.tripLog = tripLog { this.notes = rec.status; this.number = rec.correlationId }
        }
        stub.explainTripLogDecision(request)
    }
}

/** A sink and the kind of record it takes. */
class RelayShowMessageRegistration(val kind: String, val sink: RelayShowMessageWriter)
