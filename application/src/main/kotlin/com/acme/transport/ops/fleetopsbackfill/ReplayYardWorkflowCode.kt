package com.acme.transport.ops.fleetopsbackfill

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.deleteAccessPolicyRequest
import com.acme.authorization.v1.explainAccessPolicyDecisionRequest
import com.acme.billing.v1.BillingAccountServiceGrpcKt
import com.acme.billing.v1.billingAccount
import com.acme.billing.v1.createBillingAccountRequest
import com.acme.dispatch.v1.DispatchOrderWorkflowServiceGrpcKt
import com.acme.dispatch.v1.PauseDispatchOrderWorkflowRequestKt
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.pauseDispatchOrderWorkflowRequest
import com.acme.transport.kit.text.normalize
import com.acme.yards.v1.YardWorkflowServiceGrpcKt
import com.acme.yards.v1.signalYardWorkflowRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.yards.v1.YardWorkflowService/SignalYardWorkflow and hands what it keeps to the sink of its kind. */
suspend fun replayYardWorkflowCode(channel: ManagedChannel) {
    val upstream = YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub(channel)
    val reply = upstream.signalYardWorkflow(signalYardWorkflowRequest { this.reason = "live-replay-yard-workflow-code" })
    val row = ReplayYardWorkflowCodeRecord(
        code = normalize(reply.yard.code),
        name = reply.yard.name,
        referenceId = normalize(reply.outcome.referenceId),
    )
    deliverReplayYardWorkflowCode("access-policy-validation", row, channel)
}

/** Where replayYardWorkflowCode hands each record. */
interface ReplayYardWorkflowCodeDestination {
    suspend fun hand(rec: ReplayYardWorkflowCodeRecord)
}

/** What replayYardWorkflowCode keeps of each SignalYardWorkflow response. */
data class ReplayYardWorkflowCodeRecord(val code: String, val name: String, val referenceId: String)

/** Passes records to acme.dispatch.v1.DispatchOrderWorkflowService. */
class ReplayYardWorkflowCodeDispatchOrderWorkflow(private val stub: DispatchOrderWorkflowServiceGrpcKt.DispatchOrderWorkflowServiceCoroutineStub) : ReplayYardWorkflowCodeDestination {
    override suspend fun hand(rec: ReplayYardWorkflowCodeRecord) {
        val request = pauseDispatchOrderWorkflowRequest {
            this.dispatchOrder = dispatchOrder { this.dispatchOrderId = rec.code }
            this.details = PauseDispatchOrderWorkflowRequestKt.details { this.initiatedBy = rec.name }
        }
        stub.pauseDispatchOrderWorkflow(request)
    }
}

/** Writes records to acme.authorization.v1.AccessPolicyService. */
class ReplayYardWorkflowCodeAccessPolicy(private val stub: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub) : ReplayYardWorkflowCodeDestination {
    override suspend fun hand(rec: ReplayYardWorkflowCodeRecord) {
        stub.deleteAccessPolicy(deleteAccessPolicyRequest { this.reason = rec.name })
    }
}

/** Writes records to acme.authorization.v1.AccessPolicyValidationService. */
class ReplayYardWorkflowCodeAccessPolicyValidation(private val stub: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub) : ReplayYardWorkflowCodeDestination {
    override suspend fun hand(rec: ReplayYardWorkflowCodeRecord) {
        val request = explainAccessPolicyDecisionRequest {
            this.accessPolicyId = rec.name
            this.accessPolicy = accessPolicy { this.description = rec.referenceId }
            this.reason = rec.code
        }
        stub.explainAccessPolicyDecision(request)
    }
}

/** Hands records to acme.billing.v1.BillingAccountService. */
class ReplayYardWorkflowCodeBillingAccount(private val stub: BillingAccountServiceGrpcKt.BillingAccountServiceCoroutineStub) : ReplayYardWorkflowCodeDestination {
    override suspend fun hand(rec: ReplayYardWorkflowCodeRecord) {
        val request = createBillingAccountRequest {
            this.requestId = rec.code
            this.billingAccount = billingAccount { this.billingAccountId = rec.referenceId; this.currencyCode = rec.name }
        }
        stub.createBillingAccount(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class ReplayYardWorkflowCodeLogOnly : ReplayYardWorkflowCodeDestination {
    override suspend fun hand(rec: ReplayYardWorkflowCodeRecord) {
        LoggerFactory.getLogger("ReplayYardWorkflowCodeRecord").atInfo().addKeyValue("code", rec.code).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverReplayYardWorkflowCode(kind: String, rec: ReplayYardWorkflowCodeRecord, channel: ManagedChannel) {
    var sink: ReplayYardWorkflowCodeDestination = ReplayYardWorkflowCodeLogOnly()
    when (kind) {
        "dispatch-order-workflow" -> sink = ReplayYardWorkflowCodeDispatchOrderWorkflow(DispatchOrderWorkflowServiceGrpcKt.DispatchOrderWorkflowServiceCoroutineStub(channel))
        "access-policy" -> sink = ReplayYardWorkflowCodeAccessPolicy(AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub(channel))
        "access-policy-validation" -> sink = ReplayYardWorkflowCodeAccessPolicyValidation(AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub(channel))
        "billing-account" -> sink = ReplayYardWorkflowCodeBillingAccount(BillingAccountServiceGrpcKt.BillingAccountServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
