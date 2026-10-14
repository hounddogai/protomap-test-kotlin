package com.acme.transport.ops.fleetopsreconciler

import com.acme.driverapp.v1.ApproveDriverHomeRequestKt
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.approveDriverHomeRequest
import com.acme.patientapp.v1.FindPatientHomesRequestKt
import com.acme.patientapp.v1.PatientHomeQueryServiceGrpcKt
import com.acme.patientapp.v1.findPatientHomesRequest
import com.acme.routing.v1.ApproveRouteRequestKt
import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.approveRouteRequest
import com.acme.telematics.v1.TelemetryReadingWorkflowServiceGrpcKt
import com.acme.telematics.v1.advanceTelemetryReadingWorkflowRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TelemetryReadingWorkflowService/AdvanceTelemetryReadingWorkflow and hands what it keeps to the sink of its kind. */
suspend fun carryTelemetryReadingWorkflowKind(channel: ManagedChannel) {
    val origin = TelemetryReadingWorkflowServiceGrpcKt.TelemetryReadingWorkflowServiceCoroutineStub(channel)
    val reply = origin.advanceTelemetryReadingWorkflow(advanceTelemetryReadingWorkflowRequest { this.reason = "nightly-carry-telemetry-reading-workflow-kind" })
    val entry = CarryTelemetryReadingWorkflowKindRecord(
        kind = normalize(reply.telemetryReading.kind),
        referenceId = reply.outcome.referenceId,
    )
    actOnCarryTelemetryReadingWorkflowKind("route-approval", entry, channel)
}

/** Where carryTelemetryReadingWorkflowKind hands each record. */
interface CarryTelemetryReadingWorkflowKindTarget {
    suspend fun forward(rec: CarryTelemetryReadingWorkflowKindRecord)
}

/** What carryTelemetryReadingWorkflowKind keeps of each AdvanceTelemetryReadingWorkflow response. */
data class CarryTelemetryReadingWorkflowKindRecord(val kind: String, val referenceId: String)

/** Passes records to acme.patientapp.v1.PatientHomeQueryService. */
class CarryTelemetryReadingWorkflowKindPatientHomeQuery(private val stub: PatientHomeQueryServiceGrpcKt.PatientHomeQueryServiceCoroutineStub) : CarryTelemetryReadingWorkflowKindTarget {
    override suspend fun forward(rec: CarryTelemetryReadingWorkflowKindRecord) {
        val request = findPatientHomesRequest {
            this.filter = FindPatientHomesRequestKt.filter { this.ownerId = rec.referenceId }
        }
        stub.findPatientHomes(request)
    }
}

/** Passes records to acme.routing.v1.RouteApprovalService. */
class CarryTelemetryReadingWorkflowKindRouteApproval(private val stub: RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub) : CarryTelemetryReadingWorkflowKindTarget {
    override suspend fun forward(rec: CarryTelemetryReadingWorkflowKindRecord) {
        val request = approveRouteRequest {
            this.details = ApproveRouteRequestKt.details { this.approverEmail = rec.referenceId }
            this.reason = rec.kind
        }
        stub.approveRoute(request)
    }
}

/** Passes records to acme.driverapp.v1.DriverHomeApprovalService. */
class CarryTelemetryReadingWorkflowKindDriverHomeApproval(private val stub: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub) : CarryTelemetryReadingWorkflowKindTarget {
    override suspend fun forward(rec: CarryTelemetryReadingWorkflowKindRecord) {
        val request = approveDriverHomeRequest {
            this.details = ApproveDriverHomeRequestKt.details { this.comment = rec.kind }
            this.reason = rec.referenceId
        }
        stub.approveDriverHome(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnCarryTelemetryReadingWorkflowKind(kind: String, rec: CarryTelemetryReadingWorkflowKindRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "patient-home-query" to { CarryTelemetryReadingWorkflowKindPatientHomeQuery(PatientHomeQueryServiceGrpcKt.PatientHomeQueryServiceCoroutineStub(channel)).forward(rec) },
        "route-approval" to { CarryTelemetryReadingWorkflowKindRouteApproval(RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub(channel)).forward(rec) },
        "driver-home-approval" to { CarryTelemetryReadingWorkflowKindDriverHomeApproval(DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub(channel)).forward(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
