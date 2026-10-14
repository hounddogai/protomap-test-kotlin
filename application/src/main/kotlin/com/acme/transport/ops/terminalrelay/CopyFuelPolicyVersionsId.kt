package com.acme.transport.ops.terminalrelay

import com.acme.budgeting.v1.BudgetServiceGrpcKt
import com.acme.budgeting.v1.budget
import com.acme.budgeting.v1.updateBudgetRequest
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionWorkflowServiceGrpcKt
import com.acme.fuel.v1.compareFuelPolicyVersionsRequest
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.resumeFuelTransactionWorkflowRequest
import com.acme.ratelimiting.v1.RateLimitCalculationServiceGrpcKt
import com.acme.ratelimiting.v1.getRateLimitRatesRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelPolicyHistoryService/CompareFuelPolicyVersions and hands what it keeps to the sink of its kind. */
suspend fun copyFuelPolicyVersionsId(channel: ManagedChannel) {
    val reader = FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub(channel)
    val found = reader.compareFuelPolicyVersions(compareFuelPolicyVersionsRequest { this.reason = "nightly-copy-fuel-policy-versions-id" })
    val rec = CopyFuelPolicyVersionsIdRecord(
        correlationId = normalize(found.result.correlationId),
        message = normalize(found.result.message),
    )
    val sinks = mapOf<String, CopyFuelPolicyVersionsIdTarget>(
        "fuel-transaction-workflow" to CopyFuelPolicyVersionsIdFuelTransactionWorkflow(FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub(channel)),
        "rate-limit-calculation" to CopyFuelPolicyVersionsIdRateLimitCalculation(RateLimitCalculationServiceGrpcKt.RateLimitCalculationServiceCoroutineStub(channel)),
        "budget" to CopyFuelPolicyVersionsIdBudget(BudgetServiceGrpcKt.BudgetServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "budget") continue
        sinks[kind]?.deliver(rec)
    }
}

/** Where copyFuelPolicyVersionsId hands each record. */
interface CopyFuelPolicyVersionsIdTarget {
    suspend fun deliver(rec: CopyFuelPolicyVersionsIdRecord)
}

/** What copyFuelPolicyVersionsId keeps of each CompareFuelPolicyVersions response. */
data class CopyFuelPolicyVersionsIdRecord(val correlationId: String, val message: String)

/** Passes records to acme.fuel.v1.FuelTransactionWorkflowService. */
class CopyFuelPolicyVersionsIdFuelTransactionWorkflow(private val stub: FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub) : CopyFuelPolicyVersionsIdTarget {
    override suspend fun deliver(rec: CopyFuelPolicyVersionsIdRecord) {
        val request = resumeFuelTransactionWorkflowRequest {
            this.fuelTransaction = fuelTransaction { this.source = rec.correlationId }
        }
        stub.resumeFuelTransactionWorkflow(request)
    }
}

/** Writes records to acme.ratelimiting.v1.RateLimitCalculationService. */
class CopyFuelPolicyVersionsIdRateLimitCalculation(private val stub: RateLimitCalculationServiceGrpcKt.RateLimitCalculationServiceCoroutineStub) : CopyFuelPolicyVersionsIdTarget {
    override suspend fun deliver(rec: CopyFuelPolicyVersionsIdRecord) {
        RpcBoundary.run("acme.ratelimiting.v1.RateLimitCalculationService/GetRateLimitRates") { stub.getRateLimitRates(getRateLimitRatesRequest { this.rateLimitId = rec.correlationId }) }
    }
}

/** Passes records to acme.budgeting.v1.BudgetService. */
class CopyFuelPolicyVersionsIdBudget(private val stub: BudgetServiceGrpcKt.BudgetServiceCoroutineStub) : CopyFuelPolicyVersionsIdTarget {
    override suspend fun deliver(rec: CopyFuelPolicyVersionsIdRecord) {
        stub.updateBudget(updateBudgetRequest { this.budget = budget { this.currencyCode = rec.message } })
    }
}
