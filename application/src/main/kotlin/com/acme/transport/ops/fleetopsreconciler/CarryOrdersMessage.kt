package com.acme.transport.ops.fleetopsreconciler

import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.quoteProofOfDeliveryRequest
import com.acme.fuel.v1.FuelCardLifecycleServiceGrpcKt
import com.acme.fuel.v1.terminateFuelCardRequest
import com.acme.orders.v1.OrderServiceGrpcKt
import com.acme.orders.v1.listOrdersRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.orders.v1.OrderService/ListOrders and hands what it keeps to the sink of its kind. */
suspend fun carryOrdersMessage(channel: ManagedChannel) {
    val upstream = OrderServiceGrpcKt.OrderServiceCoroutineStub(channel)
    val got = upstream.listOrders(listOrdersRequest { this.orderBy = "nightly-carry-orders-message" })
    val row = CarryOrdersMessageRecord(
        message = got.result.message,
        status = got.result.status,
        correlationId = got.result.correlationId,
        nextPageToken = got.pageInfo.nextPageToken,
    )
    val sinks = mapOf<String, CarryOrdersMessageDestination>(
        "fuel-card-lifecycle" to CarryOrdersMessageFuelCardLifecycle(FuelCardLifecycleServiceGrpcKt.FuelCardLifecycleServiceCoroutineStub(channel)),
        "proof-of-delivery-calculation" to CarryOrdersMessageProofOfDeliveryCalculation(ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "proof-of-delivery-calculation") continue
        sinks[kind]?.relay(row)
    }
}

/** Where carryOrdersMessage hands each record. */
interface CarryOrdersMessageDestination {
    suspend fun relay(rec: CarryOrdersMessageRecord)
}

/** What carryOrdersMessage keeps of each ListOrders response. */
data class CarryOrdersMessageRecord(val message: String, val status: String, val correlationId: String, val nextPageToken: String)

/** Passes records to acme.fuel.v1.FuelCardLifecycleService. */
class CarryOrdersMessageFuelCardLifecycle(private val stub: FuelCardLifecycleServiceGrpcKt.FuelCardLifecycleServiceCoroutineStub) : CarryOrdersMessageDestination {
    override suspend fun relay(rec: CarryOrdersMessageRecord) {
        RpcBoundary.run("acme.fuel.v1.FuelCardLifecycleService/TerminateFuelCard") { stub.terminateFuelCard(terminateFuelCardRequest { this.fuelCardId = rec.message }) }
    }
}

/** Writes records to acme.deliveries.v1.ProofOfDeliveryCalculationService. */
class CarryOrdersMessageProofOfDeliveryCalculation(private val stub: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub) : CarryOrdersMessageDestination {
    override suspend fun relay(rec: CarryOrdersMessageRecord) {
        val request = quoteProofOfDeliveryRequest {
            this.proofOfDelivery = proofOfDelivery { this.number = rec.message; this.notes = rec.correlationId }
        }
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryCalculationService/QuoteProofOfDelivery") { stub.quoteProofOfDelivery(request) }
    }
}
