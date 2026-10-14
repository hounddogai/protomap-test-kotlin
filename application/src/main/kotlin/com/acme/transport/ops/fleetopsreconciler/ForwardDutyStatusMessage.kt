package com.acme.transport.ops.fleetopsreconciler

import com.acme.budgeting.v1.BudgetCalculationServiceGrpcKt
import com.acme.budgeting.v1.ExplainBudgetCalculationRequestKt
import com.acme.budgeting.v1.budget
import com.acme.budgeting.v1.explainBudgetCalculationRequest
import com.acme.drivers.v1.DutyStatusLifecycleServiceGrpcKt
import com.acme.drivers.v1.reinstateDutyStatusRequest
import com.acme.fleet.v1.VehicleGroupValidationServiceGrpcKt
import com.acme.fleet.v1.getVehicleGroupRulesRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.drivers.v1.DutyStatusLifecycleService/ReinstateDutyStatus and hands what it keeps to the sink of its kind. */
suspend fun forwardDutyStatusMessage(channel: ManagedChannel) {
    val reader = DutyStatusLifecycleServiceGrpcKt.DutyStatusLifecycleServiceCoroutineStub(channel)
    val got = RpcBoundary.read { reader.reinstateDutyStatus(reinstateDutyStatusRequest { this.dutyStatusId = "current-forward-duty-status-message" }) }
    val record = ForwardDutyStatusMessageRecord(
        message = got.result.message,
        status = got.result.status,
        correlationId = got.dutyStatus.correlationId,
        outcomeMessage = got.outcome.message,
    )
    actOnForwardDutyStatusMessage("budget-calculation", record, channel)
}

/** Where forwardDutyStatusMessage hands each record. */
interface ForwardDutyStatusMessageSink {
    suspend fun write(rec: ForwardDutyStatusMessageRecord)
}

/** What forwardDutyStatusMessage keeps of each ReinstateDutyStatus response. */
data class ForwardDutyStatusMessageRecord(val message: String, val status: String, val correlationId: String, val outcomeMessage: String)

/** Passes records to acme.budgeting.v1.BudgetCalculationService. */
class ForwardDutyStatusMessageBudgetCalculation(private val stub: BudgetCalculationServiceGrpcKt.BudgetCalculationServiceCoroutineStub) : ForwardDutyStatusMessageSink {
    override suspend fun write(rec: ForwardDutyStatusMessageRecord) {
        val request = explainBudgetCalculationRequest {
            this.budget = budget { this.budgetId = rec.status }
            this.details = ExplainBudgetCalculationRequestKt.details { this.locale = rec.message }
        }
        RpcBoundary.run("acme.budgeting.v1.BudgetCalculationService/ExplainBudgetCalculation") { stub.explainBudgetCalculation(request) }
    }
}

/** Passes records to acme.fleet.v1.VehicleGroupValidationService. */
class ForwardDutyStatusMessageVehicleGroupValidation(private val stub: VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub) : ForwardDutyStatusMessageSink {
    override suspend fun write(rec: ForwardDutyStatusMessageRecord) {
        stub.getVehicleGroupRules(getVehicleGroupRulesRequest { this.vehicleGroupId = rec.status })
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnForwardDutyStatusMessage(kind: String, rec: ForwardDutyStatusMessageRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "budget-calculation" to { ForwardDutyStatusMessageBudgetCalculation(BudgetCalculationServiceGrpcKt.BudgetCalculationServiceCoroutineStub(channel)).write(rec) },
        "vehicle-group-validation" to { ForwardDutyStatusMessageVehicleGroupValidation(VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub(channel)).write(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
