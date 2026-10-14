package com.acme.transport.ops.fleetopsbackfill

import com.acme.catalog.v1.ProductCalculationServiceGrpcKt
import com.acme.catalog.v1.explainProductCalculationRequest
import com.acme.customers.v1.GeneratePreferenceReportRequestKt
import com.acme.customers.v1.PreferenceReportingServiceGrpcKt
import com.acme.customers.v1.generatePreferenceReportRequest
import com.acme.fuel.v1.FuelPolicyLifecycleServiceGrpcKt
import com.acme.fuel.v1.scheduleFuelPolicyExpiryRequest
import com.acme.orders.v1.OrderServiceGrpcKt
import com.acme.orders.v1.createOrderRequest
import com.acme.orders.v1.order
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fuel.v1.FuelPolicyLifecycleService/ScheduleFuelPolicyExpiry and hands what it keeps to the sink of its kind. */
suspend fun relayFuelPolicyExpiryId(channel: ManagedChannel) {
    val upstream = FuelPolicyLifecycleServiceGrpcKt.FuelPolicyLifecycleServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.scheduleFuelPolicyExpiry(scheduleFuelPolicyExpiryRequest { this.fuelPolicyId = "live-relay-fuel-policy-expiry-id" }) }
    val entry = RelayFuelPolicyExpiryIdRecord(
        fuelPolicyId = normalize(reply.fuelPolicy.fuelPolicyId),
        correlationId = reply.result.correlationId,
        message = reply.outcome.message,
    )
    deliverRelayFuelPolicyExpiryId("product-calculation", entry, channel)
}

/** Where relayFuelPolicyExpiryId hands each record. */
interface RelayFuelPolicyExpiryIdSink {
    suspend fun forward(rec: RelayFuelPolicyExpiryIdRecord)
}

/** What relayFuelPolicyExpiryId keeps of each ScheduleFuelPolicyExpiry response. */
data class RelayFuelPolicyExpiryIdRecord(val fuelPolicyId: String, val correlationId: String, val message: String)

/** Sends records to acme.orders.v1.OrderService. */
class RelayFuelPolicyExpiryIdOrder(private val stub: OrderServiceGrpcKt.OrderServiceCoroutineStub) : RelayFuelPolicyExpiryIdSink {
    override suspend fun forward(rec: RelayFuelPolicyExpiryIdRecord) {
        val request = createOrderRequest {
            this.requestId = rec.fuelPolicyId
            this.order = order { this.notes = rec.correlationId; this.orderId = rec.message }
        }
        RpcBoundary.run("acme.orders.v1.OrderService/CreateOrder") { stub.createOrder(request) }
    }
}

/** Sends records to acme.catalog.v1.ProductCalculationService. */
class RelayFuelPolicyExpiryIdProductCalculation(private val stub: ProductCalculationServiceGrpcKt.ProductCalculationServiceCoroutineStub) : RelayFuelPolicyExpiryIdSink {
    override suspend fun forward(rec: RelayFuelPolicyExpiryIdRecord) {
        stub.explainProductCalculation(explainProductCalculationRequest { this.reason = rec.correlationId })
    }
}

/** Writes records to acme.customers.v1.PreferenceReportingService. */
class RelayFuelPolicyExpiryIdPreferenceReporting(private val stub: PreferenceReportingServiceGrpcKt.PreferenceReportingServiceCoroutineStub) : RelayFuelPolicyExpiryIdSink {
    override suspend fun forward(rec: RelayFuelPolicyExpiryIdRecord) {
        val request = generatePreferenceReportRequest {
            this.details = GeneratePreferenceReportRequestKt.details { this.format = rec.correlationId; this.cron = rec.message }
            this.preferenceId = rec.fuelPolicyId
        }
        stub.generatePreferenceReport(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class RelayFuelPolicyExpiryIdLogOnly : RelayFuelPolicyExpiryIdSink {
    override suspend fun forward(rec: RelayFuelPolicyExpiryIdRecord) {
        LoggerFactory.getLogger("RelayFuelPolicyExpiryIdRecord").atInfo().addKeyValue("fuelPolicyId", rec.fuelPolicyId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverRelayFuelPolicyExpiryId(kind: String, rec: RelayFuelPolicyExpiryIdRecord, channel: ManagedChannel) {
    var sink: RelayFuelPolicyExpiryIdSink = RelayFuelPolicyExpiryIdLogOnly()
    when (kind) {
        "order" -> sink = RelayFuelPolicyExpiryIdOrder(OrderServiceGrpcKt.OrderServiceCoroutineStub(channel))
        "product-calculation" -> sink = RelayFuelPolicyExpiryIdProductCalculation(ProductCalculationServiceGrpcKt.ProductCalculationServiceCoroutineStub(channel))
        "preference-reporting" -> sink = RelayFuelPolicyExpiryIdPreferenceReporting(PreferenceReportingServiceGrpcKt.PreferenceReportingServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
