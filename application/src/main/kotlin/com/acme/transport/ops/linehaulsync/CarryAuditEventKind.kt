package com.acme.transport.ops.linehaulsync

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.validateAuditEventRequest
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.estimateDispatchAssignmentRequest
import com.acme.drivers.v1.CompareDriverLicenseOptionsRequestKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.compareDriverLicenseOptionsRequest
import com.acme.drivers.v1.driverLicense
import com.acme.fuel.v1.CancelFuelPolicyWorkflowRequestKt
import com.acme.fuel.v1.FuelPolicyWorkflowServiceGrpcKt
import com.acme.fuel.v1.cancelFuelPolicyWorkflowRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.audit.v1.AuditEventValidationService/ValidateAuditEvent and hands what it keeps to the sink of its kind. */
suspend fun carryAuditEventKind(channel: ManagedChannel) {
    val upstream = AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel)
    val found = RpcBoundary.read { upstream.validateAuditEvent(validateAuditEventRequest { this.auditEventId = "primary-carry-audit-event-kind" }) }
    val entry = CarryAuditEventKindRecord(
        kind = normalize(found.auditEvent.kind),
        source = found.auditEvent.source,
    )
    actOnCarryAuditEventKind("driver-license-calculation", entry, channel)
}

/** Where carryAuditEventKind hands each record. */
interface CarryAuditEventKindTarget {
    suspend fun hand(rec: CarryAuditEventKindRecord)
}

/** What carryAuditEventKind keeps of each ValidateAuditEvent response. */
data class CarryAuditEventKindRecord(val kind: String, val source: String)

/** Hands records to acme.fuel.v1.FuelPolicyWorkflowService. */
class CarryAuditEventKindFuelPolicyWorkflow(private val stub: FuelPolicyWorkflowServiceGrpcKt.FuelPolicyWorkflowServiceCoroutineStub) : CarryAuditEventKindTarget {
    override suspend fun hand(rec: CarryAuditEventKindRecord) {
        val request = cancelFuelPolicyWorkflowRequest {
            this.details = CancelFuelPolicyWorkflowRequestKt.details { this.initiatedBy = rec.source }
            this.reason = rec.kind
        }
        stub.cancelFuelPolicyWorkflow(request)
    }
}

/** Writes records to acme.drivers.v1.DriverLicenseCalculationService. */
class CarryAuditEventKindDriverLicenseCalculation(private val stub: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub) : CarryAuditEventKindTarget {
    override suspend fun hand(rec: CarryAuditEventKindRecord) {
        val request = compareDriverLicenseOptionsRequest {
            this.details = CompareDriverLicenseOptionsRequestKt.details { this.currencyCode = rec.kind }
            this.driverLicense = driverLicense { this.driverLicenseId = rec.source }
        }
        stub.compareDriverLicenseOptions(request)
    }
}

/** Passes records to acme.dispatch.v1.DispatchAssignmentCalculationService. */
class CarryAuditEventKindDispatchAssignmentCalculation(private val stub: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub) : CarryAuditEventKindTarget {
    override suspend fun hand(rec: CarryAuditEventKindRecord) {
        val request = estimateDispatchAssignmentRequest {
            this.dispatchAssignment = dispatchAssignment { this.dispatchAssignmentId = rec.source }
        }
        stub.estimateDispatchAssignment(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnCarryAuditEventKind(kind: String, rec: CarryAuditEventKindRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "fuel-policy-workflow" to { CarryAuditEventKindFuelPolicyWorkflow(FuelPolicyWorkflowServiceGrpcKt.FuelPolicyWorkflowServiceCoroutineStub(channel)).hand(rec) },
        "driver-license-calculation" to { CarryAuditEventKindDriverLicenseCalculation(DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub(channel)).hand(rec) },
        "dispatch-assignment-calculation" to { CarryAuditEventKindDispatchAssignmentCalculation(DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub(channel)).hand(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
