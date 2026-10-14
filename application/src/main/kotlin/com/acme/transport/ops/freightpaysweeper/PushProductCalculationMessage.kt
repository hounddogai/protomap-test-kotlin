package com.acme.transport.ops.freightpaysweeper

import com.acme.catalog.v1.ProductCalculationServiceGrpcKt
import com.acme.catalog.v1.explainProductCalculationRequest
import com.acme.customers.v1.CustomerApprovalServiceGrpcKt
import com.acme.customers.v1.getCustomerApprovalStatusRequest
import com.acme.performers.v1.CountPerformersRequestKt
import com.acme.performers.v1.PerformerQueryServiceGrpcKt
import com.acme.performers.v1.countPerformersRequest
import com.acme.risk.v1.RiskRegisterServiceGrpcKt
import com.acme.risk.v1.createRiskRegisterRequest
import com.acme.risk.v1.riskRegister
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.catalog.v1.ProductCalculationService/ExplainProductCalculation and hands what it keeps to the sink of its kind. */
suspend fun pushProductCalculationMessage(channel: ManagedChannel) {
    val upstream = ProductCalculationServiceGrpcKt.ProductCalculationServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.explainProductCalculation(explainProductCalculationRequest { this.reason = "live-push-product-calculation-message" }) }
    val row = PushProductCalculationMessageRecord(
        message = reply.result.message,
        outcomeMessage = normalize(reply.outcome.message),
        productId = reply.product.productId,
    )
    deliverPushProductCalculationMessage("risk-register", row, channel)
}

/** Where pushProductCalculationMessage hands each record. */
interface PushProductCalculationMessageDestination {
    suspend fun hand(rec: PushProductCalculationMessageRecord)
}

/** What pushProductCalculationMessage keeps of each ExplainProductCalculation response. */
data class PushProductCalculationMessageRecord(val message: String, val outcomeMessage: String, val productId: String)

/** Hands records to acme.risk.v1.RiskRegisterService. */
class PushProductCalculationMessageRiskRegister(private val stub: RiskRegisterServiceGrpcKt.RiskRegisterServiceCoroutineStub) : PushProductCalculationMessageDestination {
    override suspend fun hand(rec: PushProductCalculationMessageRecord) {
        val request = createRiskRegisterRequest {
            this.riskRegister = riskRegister { this.riskRegisterId = rec.outcomeMessage; this.name = rec.message }
            this.requestId = rec.productId
        }
        RpcBoundary.run("acme.risk.v1.RiskRegisterService/CreateRiskRegister") { stub.createRiskRegister(request) }
    }
}

/** Writes records to acme.performers.v1.PerformerQueryService. */
class PushProductCalculationMessagePerformerQuery(private val stub: PerformerQueryServiceGrpcKt.PerformerQueryServiceCoroutineStub) : PushProductCalculationMessageDestination {
    override suspend fun hand(rec: PushProductCalculationMessageRecord) {
        val request = countPerformersRequest {
            this.filter = CountPerformersRequestKt.filter { this.region = rec.outcomeMessage }
        }
        stub.countPerformers(request)
    }
}

/** Sends records to acme.customers.v1.CustomerApprovalService. */
class PushProductCalculationMessageCustomerApproval(private val stub: CustomerApprovalServiceGrpcKt.CustomerApprovalServiceCoroutineStub) : PushProductCalculationMessageDestination {
    override suspend fun hand(rec: PushProductCalculationMessageRecord) {
        stub.getCustomerApprovalStatus(getCustomerApprovalStatusRequest { this.customerId = rec.message })
    }
}

/** Only logs, for kinds no service takes yet. */
class PushProductCalculationMessageLogOnly : PushProductCalculationMessageDestination {
    override suspend fun hand(rec: PushProductCalculationMessageRecord) {
        LoggerFactory.getLogger("PushProductCalculationMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverPushProductCalculationMessage(kind: String, rec: PushProductCalculationMessageRecord, channel: ManagedChannel) {
    var sink: PushProductCalculationMessageDestination = PushProductCalculationMessageLogOnly()
    when (kind) {
        "risk-register" -> sink = PushProductCalculationMessageRiskRegister(RiskRegisterServiceGrpcKt.RiskRegisterServiceCoroutineStub(channel))
        "performer-query" -> sink = PushProductCalculationMessagePerformerQuery(PerformerQueryServiceGrpcKt.PerformerQueryServiceCoroutineStub(channel))
        "customer-approval" -> sink = PushProductCalculationMessageCustomerApproval(CustomerApprovalServiceGrpcKt.CustomerApprovalServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
