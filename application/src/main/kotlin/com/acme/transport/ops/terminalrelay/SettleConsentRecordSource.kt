package com.acme.transport.ops.terminalrelay

import com.acme.customers.v1.ConsentRecordValidationServiceGrpcKt
import com.acme.customers.v1.scoreConsentRecordRequest
import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.delegateDispatchOrderApprovalRequest
import com.acme.dispatch.v1.recalculateZonesRequest
import com.acme.dispatch.v1.zone
import com.acme.transport.kit.text.normalize
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.startDockWorkflowRequest
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.ConsentRecordValidationService/ScoreConsentRecord and hands what it keeps to the sink of its kind. */
suspend fun settleConsentRecordSource(channel: ManagedChannel) {
    val source = ConsentRecordValidationServiceGrpcKt.ConsentRecordValidationServiceCoroutineStub(channel)
    val current = source.scoreConsentRecord(scoreConsentRecordRequest { this.consentRecordId = "nightly-settle-consent-record-source" })
    val entry = SettleConsentRecordSourceRecord(
        source = normalize(current.consentRecord.source),
        kind = current.consentRecord.kind,
    )
    actOnSettleConsentRecordSource("zone-calculation", entry, channel)
}

/** Where settleConsentRecordSource hands each record. */
interface SettleConsentRecordSourceDestination {
    suspend fun relay(rec: SettleConsentRecordSourceRecord)
}

/** What settleConsentRecordSource keeps of each ScoreConsentRecord response. */
data class SettleConsentRecordSourceRecord(val source: String, val kind: String)

/** Writes records to acme.dispatch.v1.ZoneCalculationService. */
class SettleConsentRecordSourceZoneCalculation(private val stub: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub) : SettleConsentRecordSourceDestination {
    override suspend fun relay(rec: SettleConsentRecordSourceRecord) {
        stub.recalculateZones(recalculateZonesRequest { this.zone = zone { this.zoneId = rec.kind } })
    }
}

/** Hands records to acme.yards.v1.DockWorkflowService. */
class SettleConsentRecordSourceDockWorkflow(private val stub: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub) : SettleConsentRecordSourceDestination {
    override suspend fun relay(rec: SettleConsentRecordSourceRecord) {
        stub.startDockWorkflow(startDockWorkflowRequest { this.dockId = rec.source; this.reason = rec.kind })
    }
}

/** Sends records to acme.dispatch.v1.DispatchOrderApprovalService. */
class SettleConsentRecordSourceDispatchOrderApproval(private val stub: DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub) : SettleConsentRecordSourceDestination {
    override suspend fun relay(rec: SettleConsentRecordSourceRecord) {
        stub.delegateDispatchOrderApproval(delegateDispatchOrderApprovalRequest { this.dispatchOrderId = rec.kind })
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnSettleConsentRecordSource(kind: String, rec: SettleConsentRecordSourceRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "zone-calculation" to { SettleConsentRecordSourceZoneCalculation(ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub(channel)).relay(rec) },
        "dock-workflow" to { SettleConsentRecordSourceDockWorkflow(DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub(channel)).relay(rec) },
        "dispatch-order-approval" to { SettleConsentRecordSourceDispatchOrderApproval(DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub(channel)).relay(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
