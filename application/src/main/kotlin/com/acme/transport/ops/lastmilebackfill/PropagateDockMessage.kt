package com.acme.transport.ops.lastmilebackfill

import com.acme.cart.v1.CartCalculationServiceGrpcKt
import com.acme.cart.v1.cart
import com.acme.cart.v1.estimateCartRequest
import com.acme.transport.kit.text.normalize
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.rejectDockRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.DockApprovalService/RejectDock and hands what it keeps to acme.cart.v1.CartCalculationService. */
suspend fun propagateDockMessage(channel: ManagedChannel) {
    val source = DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub(channel)
    val reply = source.rejectDock(rejectDockRequest { this.reason = "current-propagate-dock-message" })
    val rec = PropagateDockMessageRecord(
        message = normalize(reply.outcome.message),
        correlationId = reply.result.correlationId,
        code = reply.dock.code,
    )
    PropagateDockMessageCartCalculation(CartCalculationServiceGrpcKt.CartCalculationServiceCoroutineStub(channel)).transmit(rec)
}

/** What propagateDockMessage keeps of each RejectDock response. */
data class PropagateDockMessageRecord(val message: String, val correlationId: String, val code: String)

/** Writes records to acme.cart.v1.CartCalculationService. */
class PropagateDockMessageCartCalculation(private val stub: CartCalculationServiceGrpcKt.CartCalculationServiceCoroutineStub) {
    suspend fun transmit(rec: PropagateDockMessageRecord) {
        val request = estimateCartRequest {
            this.cartId = rec.code
            this.cart = cart { this.notes = rec.correlationId }
        }
        stub.estimateCart(request)
    }
}
