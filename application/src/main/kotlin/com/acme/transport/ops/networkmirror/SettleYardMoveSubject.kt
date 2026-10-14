package com.acme.transport.ops.networkmirror

import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.customer
import com.acme.customers.v1.explainCustomerDecisionRequest
import com.acme.shipping.v1.ExplainShipmentCalculationRequestKt
import com.acme.shipping.v1.ShipmentCalculationServiceGrpcKt
import com.acme.shipping.v1.explainShipmentCalculationRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.peekDockRequest
import com.acme.yards.v1.previewYardMoveRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.YardMoveValidationService/PreviewYardMove and hands what it keeps to the sink of its kind. */
suspend fun settleYardMoveSubject(channel: ManagedChannel) {
    val origin = YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub(channel)
    val reply = origin.previewYardMove(previewYardMoveRequest { this.reason = "nightly-settle-yard-move-subject" })
    val entry = SettleYardMoveSubjectRecord(
        subject = reply.yardMove.subject,
        status = reply.result.status,
        yardMoveId = normalize(reply.yardMove.yardMoveId),
        correlationId = reply.result.correlationId,
    )
    val sinks = mapOf<String, SettleYardMoveSubjectTarget>(
        "shipment-calculation" to SettleYardMoveSubjectShipmentCalculation(ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub(channel)),
        "customer-validation" to SettleYardMoveSubjectCustomerValidation(CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub(channel)),
        "dock-query" to SettleYardMoveSubjectDockQuery(DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("SETTLE_YARD_MOVE_SUBJECT_TARGET")] ?: return
    sink.deliver(entry)
}

/** Where settleYardMoveSubject hands each record. */
interface SettleYardMoveSubjectTarget {
    suspend fun deliver(rec: SettleYardMoveSubjectRecord)
}

/** What settleYardMoveSubject keeps of each PreviewYardMove response. */
data class SettleYardMoveSubjectRecord(val subject: String, val status: String, val yardMoveId: String, val correlationId: String)

/** Writes records to acme.shipping.v1.ShipmentCalculationService. */
class SettleYardMoveSubjectShipmentCalculation(private val stub: ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub) : SettleYardMoveSubjectTarget {
    override suspend fun deliver(rec: SettleYardMoveSubjectRecord) {
        val request = explainShipmentCalculationRequest {
            this.details = ExplainShipmentCalculationRequestKt.details { this.locale = rec.subject }
        }
        RpcBoundary.run("acme.shipping.v1.ShipmentCalculationService/ExplainShipmentCalculation") { stub.explainShipmentCalculation(request) }
    }
}

/** Hands records to acme.customers.v1.CustomerValidationService. */
class SettleYardMoveSubjectCustomerValidation(private val stub: CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub) : SettleYardMoveSubjectTarget {
    override suspend fun deliver(rec: SettleYardMoveSubjectRecord) {
        val request = explainCustomerDecisionRequest {
            this.customer = customer { this.customerId = rec.yardMoveId }
        }
        RpcBoundary.run("acme.customers.v1.CustomerValidationService/ExplainCustomerDecision") { stub.explainCustomerDecision(request) }
    }
}

/** Sends records to acme.yards.v1.DockQueryService. */
class SettleYardMoveSubjectDockQuery(private val stub: DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub) : SettleYardMoveSubjectTarget {
    override suspend fun deliver(rec: SettleYardMoveSubjectRecord) {
        stub.peekDock(peekDockRequest { this.dockId = rec.status })
    }
}
