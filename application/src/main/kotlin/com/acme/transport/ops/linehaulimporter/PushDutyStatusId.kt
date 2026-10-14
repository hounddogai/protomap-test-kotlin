package com.acme.transport.ops.linehaulimporter

import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.quoteDutyStatusRequest
import com.acme.fuel.v1.FuelPolicySyncServiceGrpcKt
import com.acme.fuel.v1.ListFuelPolicyConflictsRequestKt
import com.acme.fuel.v1.listFuelPolicyConflictsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.getYardApprovalStatusRequest
import io.grpc.ManagedChannel

/** Reads acme.drivers.v1.DutyStatusCalculationService/QuoteDutyStatus and hands what it keeps to the sink of its kind. */
suspend fun pushDutyStatusId(channel: ManagedChannel) {
    val source = DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub(channel)
    val found = RpcBoundary.read { source.quoteDutyStatus(quoteDutyStatusRequest { this.reason = "live-push-duty-status-id" }) }
    val record = PushDutyStatusIdRecord(
        correlationId = found.dutyStatus.correlationId,
        status = normalize(found.result.status),
    )
    actOnPushDutyStatusId("yard-approval", record, channel)
}

/** Where pushDutyStatusId hands each record. */
interface PushDutyStatusIdWriter {
    suspend fun relay(rec: PushDutyStatusIdRecord)
}

/** What pushDutyStatusId keeps of each QuoteDutyStatus response. */
data class PushDutyStatusIdRecord(val correlationId: String, val status: String)

/** Writes records to acme.yards.v1.YardApprovalService. */
class PushDutyStatusIdYardApproval(private val stub: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub) : PushDutyStatusIdWriter {
    override suspend fun relay(rec: PushDutyStatusIdRecord) {
        stub.getYardApprovalStatus(getYardApprovalStatusRequest { this.yardId = rec.correlationId })
    }
}

/** Writes records to acme.fuel.v1.FuelPolicySyncService. */
class PushDutyStatusIdFuelPolicySync(private val stub: FuelPolicySyncServiceGrpcKt.FuelPolicySyncServiceCoroutineStub) : PushDutyStatusIdWriter {
    override suspend fun relay(rec: PushDutyStatusIdRecord) {
        val request = listFuelPolicyConflictsRequest {
            this.filter = ListFuelPolicyConflictsRequestKt.filter { this.ownerId = rec.status }
        }
        stub.listFuelPolicyConflicts(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnPushDutyStatusId(kind: String, rec: PushDutyStatusIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "yard-approval" to { PushDutyStatusIdYardApproval(YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub(channel)).relay(rec) },
        "fuel-policy-sync" to { PushDutyStatusIdFuelPolicySync(FuelPolicySyncServiceGrpcKt.FuelPolicySyncServiceCoroutineStub(channel)).relay(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
