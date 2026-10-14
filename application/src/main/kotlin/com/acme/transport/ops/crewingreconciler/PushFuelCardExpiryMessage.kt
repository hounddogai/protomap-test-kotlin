package com.acme.transport.ops.crewingreconciler

import com.acme.dispatch.v1.DispatchOrderValidationServiceGrpcKt
import com.acme.dispatch.v1.LintDispatchOrdersRequestKt
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.lintDispatchOrdersRequest
import com.acme.fuel.v1.FuelCardLifecycleServiceGrpcKt
import com.acme.fuel.v1.scheduleFuelCardExpiryRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelCardLifecycleService/ScheduleFuelCardExpiry and hands what it keeps to acme.dispatch.v1.DispatchOrderValidationService. */
suspend fun pushFuelCardExpiryMessage(channel: ManagedChannel) {
    val reader = FuelCardLifecycleServiceGrpcKt.FuelCardLifecycleServiceCoroutineStub(channel)
    val got = RpcBoundary.read { reader.scheduleFuelCardExpiry(scheduleFuelCardExpiryRequest { this.reason = "primary-push-fuel-card-expiry-message" }) }
    val entry = PushFuelCardExpiryMessageRecord(
        message = normalize(got.result.message),
        currencyCode = got.fuelCard.currencyCode,
        correlationId = got.result.correlationId,
    )
    PushFuelCardExpiryMessageDispatchOrderValidation(DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineStub(channel)).deliver(entry)
}

/** What pushFuelCardExpiryMessage keeps of each ScheduleFuelCardExpiry response. */
data class PushFuelCardExpiryMessageRecord(val message: String, val currencyCode: String, val correlationId: String)

/** Writes records to acme.dispatch.v1.DispatchOrderValidationService. */
class PushFuelCardExpiryMessageDispatchOrderValidation(private val stub: DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineStub) {
    suspend fun deliver(rec: PushFuelCardExpiryMessageRecord) {
        val request = lintDispatchOrdersRequest {
            this.dispatchOrder = dispatchOrder { this.notes = rec.currencyCode }
            this.details = LintDispatchOrdersRequestKt.details { this.locale = rec.message }
        }
        stub.lintDispatchOrders(request)
    }
}
