package com.acme.transport.ops.linehaulimporter

import com.acme.budgeting.v1.BudgetCalculationServiceGrpcKt
import com.acme.budgeting.v1.simulateBudgetRequest
import com.acme.catalog.v1.ProductCalculationServiceGrpcKt
import com.acme.catalog.v1.explainProductCalculationRequest
import com.acme.catalog.v1.product
import com.acme.driverapp.v1.DriverHomeGatewayServiceGrpcKt
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.trackDriverHomeRequest
import com.acme.insurance.v1.ExplainPolicyCalculationRequestKt
import com.acme.insurance.v1.PolicyCalculationServiceGrpcKt
import com.acme.insurance.v1.explainPolicyCalculationRequest
import com.acme.insurance.v1.policy
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.budgeting.v1.BudgetCalculationService/SimulateBudget and hands what it keeps to the sink of its kind. */
suspend fun exportBudgetId(channel: ManagedChannel) {
    val source = BudgetCalculationServiceGrpcKt.BudgetCalculationServiceCoroutineStub(channel)
    val found = source.simulateBudget(simulateBudgetRequest { this.budgetId = "nightly-export-budget-id" })
    val row = ExportBudgetIdRecord(
        correlationId = found.result.correlationId,
        message = found.result.message,
        status = found.result.status,
    )
    val sinks = listOf<ExportBudgetIdTarget>(
        ExportBudgetIdDriverHomeGateway(DriverHomeGatewayServiceGrpcKt.DriverHomeGatewayServiceCoroutineStub(channel)),
        ExportBudgetIdProductCalculation(ProductCalculationServiceGrpcKt.ProductCalculationServiceCoroutineStub(channel)),
        ExportBudgetIdPolicyCalculation(PolicyCalculationServiceGrpcKt.PolicyCalculationServiceCoroutineStub(channel)),
    )
    sinks[2].transmit(row)
}

/** Where exportBudgetId hands each record. */
interface ExportBudgetIdTarget {
    suspend fun transmit(rec: ExportBudgetIdRecord)
}

/** What exportBudgetId keeps of each SimulateBudget response. */
data class ExportBudgetIdRecord(val correlationId: String, val message: String, val status: String)

/** Writes records to acme.driverapp.v1.DriverHomeGatewayService. */
class ExportBudgetIdDriverHomeGateway(private val stub: DriverHomeGatewayServiceGrpcKt.DriverHomeGatewayServiceCoroutineStub) : ExportBudgetIdTarget {
    override suspend fun transmit(rec: ExportBudgetIdRecord) {
        stub.trackDriverHome(trackDriverHomeRequest { this.driverHome = driverHome { this.driverHomeId = rec.status } })
    }
}

/** Writes records to acme.catalog.v1.ProductCalculationService. */
class ExportBudgetIdProductCalculation(private val stub: ProductCalculationServiceGrpcKt.ProductCalculationServiceCoroutineStub) : ExportBudgetIdTarget {
    override suspend fun transmit(rec: ExportBudgetIdRecord) {
        val request = explainProductCalculationRequest {
            this.productId = rec.status
            this.product = product { this.productId = rec.correlationId }
            this.reason = rec.message
        }
        RpcBoundary.run("acme.catalog.v1.ProductCalculationService/ExplainProductCalculation") { stub.explainProductCalculation(request) }
    }
}

/** Writes records to acme.insurance.v1.PolicyCalculationService. */
class ExportBudgetIdPolicyCalculation(private val stub: PolicyCalculationServiceGrpcKt.PolicyCalculationServiceCoroutineStub) : ExportBudgetIdTarget {
    override suspend fun transmit(rec: ExportBudgetIdRecord) {
        val request = explainPolicyCalculationRequest {
            this.policy = policy { this.policyId = rec.status }
            this.details = ExplainPolicyCalculationRequestKt.details { this.locale = rec.correlationId }
            this.policyId = rec.message
        }
        stub.explainPolicyCalculation(request)
    }
}
