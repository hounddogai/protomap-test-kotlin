package com.acme.transport.ops.fleetopsreconciler

import com.acme.concessions.v1.ConcessionStandServiceGrpcKt
import com.acme.concessions.v1.cloneConcessionStandRequest
import com.acme.concessions.v1.concessionStand
import com.acme.customers.v1.CustomerSegmentApprovalServiceGrpcKt
import com.acme.customers.v1.PreferenceCalculationServiceGrpcKt
import com.acme.customers.v1.QuotePreferenceRequestKt
import com.acme.customers.v1.approveCustomerSegmentRequest
import com.acme.customers.v1.quotePreferenceRequest
import com.acme.dispatch.v1.DispatchAssignmentWorkflowServiceGrpcKt
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.startDispatchAssignmentWorkflowRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.customers.v1.CustomerSegmentApprovalService/ApproveCustomerSegment and hands what it keeps to the sink of its kind. */
suspend fun mirrorCustomerSegmentMessage(channel: ManagedChannel) {
    val upstream = CustomerSegmentApprovalServiceGrpcKt.CustomerSegmentApprovalServiceCoroutineStub(channel)
    val found = upstream.approveCustomerSegment(approveCustomerSegmentRequest { this.customerSegmentId = "live-mirror-customer-segment-message" })
    val record = MirrorCustomerSegmentMessageRecord(
        message = normalize(found.outcome.message),
        status = found.result.status,
    )
    deliverMirrorCustomerSegmentMessage("concession-stand", record, channel)
}

/** Where mirrorCustomerSegmentMessage hands each record. */
interface MirrorCustomerSegmentMessageWriter {
    suspend fun forward(rec: MirrorCustomerSegmentMessageRecord)
}

/** What mirrorCustomerSegmentMessage keeps of each ApproveCustomerSegment response. */
data class MirrorCustomerSegmentMessageRecord(val message: String, val status: String)

/** Hands records to acme.customers.v1.PreferenceCalculationService. */
class MirrorCustomerSegmentMessagePreferenceCalculation(private val stub: PreferenceCalculationServiceGrpcKt.PreferenceCalculationServiceCoroutineStub) : MirrorCustomerSegmentMessageWriter {
    override suspend fun forward(rec: MirrorCustomerSegmentMessageRecord) {
        val request = quotePreferenceRequest {
            this.details = QuotePreferenceRequestKt.details { this.currencyCode = rec.status }
        }
        stub.quotePreference(request)
    }
}

/** Hands records to acme.dispatch.v1.DispatchAssignmentWorkflowService. */
class MirrorCustomerSegmentMessageDispatchAssignmentWorkflow(private val stub: DispatchAssignmentWorkflowServiceGrpcKt.DispatchAssignmentWorkflowServiceCoroutineStub) : MirrorCustomerSegmentMessageWriter {
    override suspend fun forward(rec: MirrorCustomerSegmentMessageRecord) {
        val request = startDispatchAssignmentWorkflowRequest {
            this.dispatchAssignment = dispatchAssignment { this.dispatchAssignmentId = rec.status }
        }
        stub.startDispatchAssignmentWorkflow(request)
    }
}

/** Sends records to acme.concessions.v1.ConcessionStandService. */
class MirrorCustomerSegmentMessageConcessionStand(private val stub: ConcessionStandServiceGrpcKt.ConcessionStandServiceCoroutineStub) : MirrorCustomerSegmentMessageWriter {
    override suspend fun forward(rec: MirrorCustomerSegmentMessageRecord) {
        val request = cloneConcessionStandRequest {
            this.concessionStand = concessionStand { this.code = rec.status; this.concessionStandId = rec.message }
        }
        stub.cloneConcessionStand(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class MirrorCustomerSegmentMessageLogOnly : MirrorCustomerSegmentMessageWriter {
    override suspend fun forward(rec: MirrorCustomerSegmentMessageRecord) {
        LoggerFactory.getLogger("MirrorCustomerSegmentMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverMirrorCustomerSegmentMessage(kind: String, rec: MirrorCustomerSegmentMessageRecord, channel: ManagedChannel) {
    var sink: MirrorCustomerSegmentMessageWriter = MirrorCustomerSegmentMessageLogOnly()
    when (kind) {
        "preference-calculation" -> sink = MirrorCustomerSegmentMessagePreferenceCalculation(PreferenceCalculationServiceGrpcKt.PreferenceCalculationServiceCoroutineStub(channel))
        "dispatch-assignment-workflow" -> sink = MirrorCustomerSegmentMessageDispatchAssignmentWorkflow(DispatchAssignmentWorkflowServiceGrpcKt.DispatchAssignmentWorkflowServiceCoroutineStub(channel))
        "concession-stand" -> sink = MirrorCustomerSegmentMessageConcessionStand(ConcessionStandServiceGrpcKt.ConcessionStandServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
