package com.acme.transport.ops.fleetopsreconciler

import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.compareDriverLicenseOptionsRequest
import com.acme.drivers.v1.driverLicense
import com.acme.fleet.v1.InspectionValidationServiceGrpcKt
import com.acme.fleet.v1.NormalizeInspectionRequestKt
import com.acme.fleet.v1.normalizeInspectionRequest
import com.acme.maintenance.v1.PartValidationServiceGrpcKt
import com.acme.maintenance.v1.explainPartDecisionRequest
import com.acme.maintenance.v1.part
import com.acme.routing.v1.StopWorkflowServiceGrpcKt
import com.acme.routing.v1.resumeStopWorkflowRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.routing.v1.StopWorkflowService/ResumeStopWorkflow and hands what it keeps to the sink of its kind. */
suspend fun propagateStopWorkflowId(channel: ManagedChannel) {
    val reader = StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub(channel)
    val got = reader.resumeStopWorkflow(resumeStopWorkflowRequest { this.reason = "current-propagate-stop-workflow-id" })
    val record = PropagateStopWorkflowIdRecord(
        correlationId = got.result.correlationId,
        message = got.outcome.message,
    )
    dispatchPropagateStopWorkflowId("inspection-validation", record, channel)
}

/** Where propagateStopWorkflowId hands each record. */
interface PropagateStopWorkflowIdTarget {
    suspend fun deliver(rec: PropagateStopWorkflowIdRecord)
}

/** What propagateStopWorkflowId keeps of each ResumeStopWorkflow response. */
data class PropagateStopWorkflowIdRecord(val correlationId: String, val message: String)

/** Hands records to acme.maintenance.v1.PartValidationService. */
class PropagateStopWorkflowIdPartValidation(private val stub: PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub) : PropagateStopWorkflowIdTarget {
    override suspend fun deliver(rec: PropagateStopWorkflowIdRecord) {
        RpcBoundary.run("acme.maintenance.v1.PartValidationService/ExplainPartDecision") { stub.explainPartDecision(explainPartDecisionRequest { this.part = part { this.code = rec.correlationId } }) }
    }
}

/** Hands records to acme.drivers.v1.DriverLicenseCalculationService. */
class PropagateStopWorkflowIdDriverLicenseCalculation(private val stub: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub) : PropagateStopWorkflowIdTarget {
    override suspend fun deliver(rec: PropagateStopWorkflowIdRecord) {
        val request = compareDriverLicenseOptionsRequest {
            this.driverLicense = driverLicense { this.notes = rec.correlationId }
        }
        RpcBoundary.run("acme.drivers.v1.DriverLicenseCalculationService/CompareDriverLicenseOptions") { stub.compareDriverLicenseOptions(request) }
    }
}

/** Passes records to acme.fleet.v1.InspectionValidationService. */
class PropagateStopWorkflowIdInspectionValidation(private val stub: InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub) : PropagateStopWorkflowIdTarget {
    override suspend fun deliver(rec: PropagateStopWorkflowIdRecord) {
        val request = normalizeInspectionRequest {
            this.inspectionId = rec.correlationId
            this.details = NormalizeInspectionRequestKt.details { this.locale = rec.message }
        }
        stub.normalizeInspection(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class PropagateStopWorkflowIdLogOnly : PropagateStopWorkflowIdTarget {
    override suspend fun deliver(rec: PropagateStopWorkflowIdRecord) {
        LoggerFactory.getLogger("PropagateStopWorkflowIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchPropagateStopWorkflowId(kind: String, rec: PropagateStopWorkflowIdRecord, channel: ManagedChannel) {
    var write: suspend (PropagateStopWorkflowIdRecord) -> Unit = PropagateStopWorkflowIdLogOnly()::deliver
    when (kind) {
        "part-validation" -> write = PropagateStopWorkflowIdPartValidation(PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub(channel))::deliver
        "driver-license-calculation" -> write = PropagateStopWorkflowIdDriverLicenseCalculation(DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub(channel))::deliver
        "inspection-validation" -> write = PropagateStopWorkflowIdInspectionValidation(InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub(channel))::deliver
    }
    write(rec)
}
