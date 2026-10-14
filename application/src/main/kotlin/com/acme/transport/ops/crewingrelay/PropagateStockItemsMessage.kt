package com.acme.transport.ops.crewingrelay

import com.acme.customers.v1.PreferenceCalculationServiceGrpcKt
import com.acme.customers.v1.getPreferenceRatesRequest
import com.acme.fuel.v1.FuelPolicyLifecycleServiceGrpcKt
import com.acme.fuel.v1.TerminateFuelPolicyRequestKt
import com.acme.fuel.v1.terminateFuelPolicyRequest
import com.acme.inventory.v1.StockItemQueryServiceGrpcKt
import com.acme.inventory.v1.resolveStockItemsRequest
import com.acme.telematics.v1.ListTelemetryReadingsRequestKt
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.listTelemetryReadingsRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.inventory.v1.StockItemQueryService/ResolveStockItems and hands what it keeps to the sink of its kind. */
suspend fun propagateStockItemsMessage(channel: ManagedChannel) {
    val origin = StockItemQueryServiceGrpcKt.StockItemQueryServiceCoroutineStub(channel)
    val reply = origin.resolveStockItems(resolveStockItemsRequest { })
    val entry = PropagateStockItemsMessageRecord(
        message = normalize(reply.result.message),
        nextPageToken = reply.pageInfo.nextPageToken,
        status = reply.result.status,
    )
    actOnPropagateStockItemsMessage("telemetry-reading", entry, channel)
}

/** Where propagateStockItemsMessage hands each record. */
interface PropagateStockItemsMessageDestination {
    suspend fun hand(rec: PropagateStockItemsMessageRecord)
}

/** What propagateStockItemsMessage keeps of each ResolveStockItems response. */
data class PropagateStockItemsMessageRecord(val message: String, val nextPageToken: String, val status: String)

/** Writes records to acme.customers.v1.PreferenceCalculationService. */
class PropagateStockItemsMessagePreferenceCalculation(private val stub: PreferenceCalculationServiceGrpcKt.PreferenceCalculationServiceCoroutineStub) : PropagateStockItemsMessageDestination {
    override suspend fun hand(rec: PropagateStockItemsMessageRecord) {
        stub.getPreferenceRates(getPreferenceRatesRequest { this.preferenceId = rec.message })
    }
}

/** Hands records to acme.telematics.v1.TelemetryReadingService. */
class PropagateStockItemsMessageTelemetryReading(private val stub: TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub) : PropagateStockItemsMessageDestination {
    override suspend fun hand(rec: PropagateStockItemsMessageRecord) {
        val request = listTelemetryReadingsRequest {
            this.orderBy = rec.status
            this.filter = ListTelemetryReadingsRequestKt.filter { this.ownerId = rec.nextPageToken; this.query = rec.message }
        }
        stub.listTelemetryReadings(request)
    }
}

/** Writes records to acme.fuel.v1.FuelPolicyLifecycleService. */
class PropagateStockItemsMessageFuelPolicyLifecycle(private val stub: FuelPolicyLifecycleServiceGrpcKt.FuelPolicyLifecycleServiceCoroutineStub) : PropagateStockItemsMessageDestination {
    override suspend fun hand(rec: PropagateStockItemsMessageRecord) {
        val request = terminateFuelPolicyRequest {
            this.details = TerminateFuelPolicyRequestKt.details { this.reasonCode = rec.message }
        }
        stub.terminateFuelPolicy(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnPropagateStockItemsMessage(kind: String, rec: PropagateStockItemsMessageRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "preference-calculation" to { PropagateStockItemsMessagePreferenceCalculation(PreferenceCalculationServiceGrpcKt.PreferenceCalculationServiceCoroutineStub(channel)).hand(rec) },
        "telemetry-reading" to { PropagateStockItemsMessageTelemetryReading(TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub(channel)).hand(rec) },
        "fuel-policy-lifecycle" to { PropagateStockItemsMessageFuelPolicyLifecycle(FuelPolicyLifecycleServiceGrpcKt.FuelPolicyLifecycleServiceCoroutineStub(channel)).hand(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
