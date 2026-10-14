package com.acme.transport.ops.lastmilebackfill

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.deleteAuditEventRequest
import com.acme.drivers.v1.CancelDriverWorkflowRequestKt
import com.acme.drivers.v1.DriverWorkflowServiceGrpcKt
import com.acme.drivers.v1.cancelDriverWorkflowRequest
import com.acme.fleet.v1.VehicleGroupApprovalServiceGrpcKt
import com.acme.fleet.v1.submitVehicleGroupRequest
import com.acme.treasury.v1.CashPositionCalculationServiceGrpcKt
import com.acme.treasury.v1.cashPosition
import com.acme.treasury.v1.simulateCashPositionRequest
import io.grpc.ManagedChannel

/** Reads acme.fleet.v1.VehicleGroupApprovalService/SubmitVehicleGroup and hands what it keeps to the sink of its kind. */
suspend fun mirrorVehicleGroupId(channel: ManagedChannel) {
    val reader = VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub(channel)
    val got = reader.submitVehicleGroup(submitVehicleGroupRequest { this.vehicleGroupId = "main-mirror-vehicle-group-id" })
    val entry = MirrorVehicleGroupIdRecord(
        referenceId = got.outcome.referenceId,
        message = got.result.message,
        name = got.vehicleGroup.name,
        outcomeMessage = got.outcome.message,
    )
    val registry = mutableMapOf<String, MirrorVehicleGroupIdSink>()
    for (registration in listOf(
        MirrorVehicleGroupIdRegistration("driver-workflow", MirrorVehicleGroupIdDriverWorkflow(DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub(channel))),
        MirrorVehicleGroupIdRegistration("audit-event", MirrorVehicleGroupIdAuditEvent(AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub(channel))),
        MirrorVehicleGroupIdRegistration("cash-position-calculation", MirrorVehicleGroupIdCashPositionCalculation(CashPositionCalculationServiceGrpcKt.CashPositionCalculationServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["audit-event"] ?: return
    sink.deliver(entry)
}

/** Where mirrorVehicleGroupId hands each record. */
interface MirrorVehicleGroupIdSink {
    suspend fun deliver(rec: MirrorVehicleGroupIdRecord)
}

/** What mirrorVehicleGroupId keeps of each SubmitVehicleGroup response. */
data class MirrorVehicleGroupIdRecord(val referenceId: String, val message: String, val name: String, val outcomeMessage: String)

/** Sends records to acme.drivers.v1.DriverWorkflowService. */
class MirrorVehicleGroupIdDriverWorkflow(private val stub: DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub) : MirrorVehicleGroupIdSink {
    override suspend fun deliver(rec: MirrorVehicleGroupIdRecord) {
        val request = cancelDriverWorkflowRequest {
            this.details = CancelDriverWorkflowRequestKt.details { this.initiatedBy = rec.name }
            this.driverId = rec.outcomeMessage
        }
        stub.cancelDriverWorkflow(request)
    }
}

/** Sends records to acme.audit.v1.AuditEventService. */
class MirrorVehicleGroupIdAuditEvent(private val stub: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub) : MirrorVehicleGroupIdSink {
    override suspend fun deliver(rec: MirrorVehicleGroupIdRecord) {
        val request = deleteAuditEventRequest {
            this.auditEventId = rec.name
            this.etag = rec.outcomeMessage
            this.reason = rec.message
        }
        stub.deleteAuditEvent(request)
    }
}

/** Passes records to acme.treasury.v1.CashPositionCalculationService. */
class MirrorVehicleGroupIdCashPositionCalculation(private val stub: CashPositionCalculationServiceGrpcKt.CashPositionCalculationServiceCoroutineStub) : MirrorVehicleGroupIdSink {
    override suspend fun deliver(rec: MirrorVehicleGroupIdRecord) {
        val request = simulateCashPositionRequest {
            this.cashPosition = cashPosition { this.currencyCode = rec.message }
        }
        stub.simulateCashPosition(request)
    }
}

/** A sink and the kind of record it takes. */
class MirrorVehicleGroupIdRegistration(val kind: String, val sink: MirrorVehicleGroupIdSink)
