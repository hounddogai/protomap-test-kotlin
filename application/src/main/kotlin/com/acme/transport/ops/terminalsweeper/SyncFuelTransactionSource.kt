package com.acme.transport.ops.terminalsweeper

import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.SearchDeliveriesRequestKt
import com.acme.deliveries.v1.searchDeliveriesRequest
import com.acme.drivers.v1.DriverWorkflowServiceGrpcKt
import com.acme.drivers.v1.SignalDriverWorkflowRequestKt
import com.acme.drivers.v1.signalDriverWorkflowRequest
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.peekFuelTransactionRequest
import com.acme.telematics.v1.TelemetryReadingSyncServiceGrpcKt
import com.acme.telematics.v1.pullTelemetryReadingsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelTransactionQueryService/PeekFuelTransaction and hands what it keeps to the sink of its kind. */
suspend fun syncFuelTransactionSource(channel: ManagedChannel) {
    val upstream = FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub(channel)
    val current = RpcBoundary.read { upstream.peekFuelTransaction(peekFuelTransactionRequest { this.fuelTransactionId = "default-sync-fuel-transaction-source" }) }
    val rec = SyncFuelTransactionSourceRecord(
        source = current.fuelTransaction.source,
        correlationId = current.result.correlationId,
    )
    val registry = mutableMapOf<String, SyncFuelTransactionSourceDestination>()
    for (registration in listOf(
        SyncFuelTransactionSourceRegistration("telemetry-reading-sync", SyncFuelTransactionSourceTelemetryReadingSync(TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub(channel))),
        SyncFuelTransactionSourceRegistration("delivery", SyncFuelTransactionSourceDelivery(DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub(channel))),
        SyncFuelTransactionSourceRegistration("driver-workflow", SyncFuelTransactionSourceDriverWorkflow(DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["delivery"] ?: return
    sink.deliver(rec)
}

/** Where syncFuelTransactionSource hands each record. */
interface SyncFuelTransactionSourceDestination {
    suspend fun deliver(rec: SyncFuelTransactionSourceRecord)
}

/** What syncFuelTransactionSource keeps of each PeekFuelTransaction response. */
data class SyncFuelTransactionSourceRecord(val source: String, val correlationId: String)

/** Sends records to acme.telematics.v1.TelemetryReadingSyncService. */
class SyncFuelTransactionSourceTelemetryReadingSync(private val stub: TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub) : SyncFuelTransactionSourceDestination {
    override suspend fun deliver(rec: SyncFuelTransactionSourceRecord) {
        stub.pullTelemetryReadings(pullTelemetryReadingsRequest { this.telemetryReadingId = rec.source })
    }
}

/** Writes records to acme.deliveries.v1.DeliveryService. */
class SyncFuelTransactionSourceDelivery(private val stub: DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub) : SyncFuelTransactionSourceDestination {
    override suspend fun deliver(rec: SyncFuelTransactionSourceRecord) {
        val request = searchDeliveriesRequest {
            this.filter = SearchDeliveriesRequestKt.filter { this.region = rec.correlationId; this.query = rec.source }
        }
        RpcBoundary.run("acme.deliveries.v1.DeliveryService/SearchDeliveries") { stub.searchDeliveries(request) }
    }
}

/** Sends records to acme.drivers.v1.DriverWorkflowService. */
class SyncFuelTransactionSourceDriverWorkflow(private val stub: DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub) : SyncFuelTransactionSourceDestination {
    override suspend fun deliver(rec: SyncFuelTransactionSourceRecord) {
        val request = signalDriverWorkflowRequest {
            this.reason = rec.source
            this.details = SignalDriverWorkflowRequestKt.details { this.step = rec.correlationId }
        }
        stub.signalDriverWorkflow(request)
    }
}

/** A sink and the kind of record it takes. */
class SyncFuelTransactionSourceRegistration(val kind: String, val sink: SyncFuelTransactionSourceDestination)
