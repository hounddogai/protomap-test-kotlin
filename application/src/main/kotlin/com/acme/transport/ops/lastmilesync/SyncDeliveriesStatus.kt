package com.acme.transport.ops.lastmilesync

import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.ListPendingProofOfDeliveriesRequestKt
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.listPendingProofOfDeliveriesRequest
import com.acme.deliveries.v1.resolveDeliveriesRequest
import com.acme.telematics.v1.TelemetryReadingCalculationServiceGrpcKt
import com.acme.telematics.v1.getTelemetryReadingRatesRequest
import com.acme.tours.v1.TourServiceGrpcKt
import com.acme.tours.v1.deleteTourRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.deliveries.v1.DeliveryQueryService/ResolveDeliveries and hands what it keeps to the sink of its kind. */
suspend fun syncDeliveriesStatus(channel: ManagedChannel) {
    val source = DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub(channel)
    val current = source.resolveDeliveries(resolveDeliveriesRequest { })
    val entry = SyncDeliveriesStatusRecord(
        status = current.result.status,
        correlationId = current.result.correlationId,
        message = current.result.message,
    )
    val sinks = mapOf<String, SyncDeliveriesStatusWriter>(
        "telemetry-reading-calculation" to SyncDeliveriesStatusTelemetryReadingCalculation(TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub(channel)),
        "tour" to SyncDeliveriesStatusTour(TourServiceGrpcKt.TourServiceCoroutineStub(channel)),
        "proof-of-delivery-approval" to SyncDeliveriesStatusProofOfDeliveryApproval(ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub(channel)),
    )
    routeSyncDeliveriesStatus(sinks, "tour", entry)
}

/** Where syncDeliveriesStatus hands each record. */
interface SyncDeliveriesStatusWriter {
    suspend fun relay(rec: SyncDeliveriesStatusRecord)
}

/** What syncDeliveriesStatus keeps of each ResolveDeliveries response. */
data class SyncDeliveriesStatusRecord(val status: String, val correlationId: String, val message: String)

/** Sends records to acme.telematics.v1.TelemetryReadingCalculationService. */
class SyncDeliveriesStatusTelemetryReadingCalculation(private val stub: TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub) : SyncDeliveriesStatusWriter {
    override suspend fun relay(rec: SyncDeliveriesStatusRecord) {
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingCalculationService/GetTelemetryReadingRates") { stub.getTelemetryReadingRates(getTelemetryReadingRatesRequest { this.telemetryReadingId = rec.status }) }
    }
}

/** Writes records to acme.tours.v1.TourService. */
class SyncDeliveriesStatusTour(private val stub: TourServiceGrpcKt.TourServiceCoroutineStub) : SyncDeliveriesStatusWriter {
    override suspend fun relay(rec: SyncDeliveriesStatusRecord) {
        stub.deleteTour(deleteTourRequest { this.tourId = rec.message })
    }
}

/** Passes records to acme.deliveries.v1.ProofOfDeliveryApprovalService. */
class SyncDeliveriesStatusProofOfDeliveryApproval(private val stub: ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub) : SyncDeliveriesStatusWriter {
    override suspend fun relay(rec: SyncDeliveriesStatusRecord) {
        val request = listPendingProofOfDeliveriesRequest {
            this.orderBy = rec.correlationId
            this.filter = ListPendingProofOfDeliveriesRequestKt.filter { this.ownerId = rec.status; this.region = rec.message }
        }
        stub.listPendingProofOfDeliveries(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeSyncDeliveriesStatus(sinks: Map<String, SyncDeliveriesStatusWriter>, kind: String, rec: SyncDeliveriesStatusRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.relay(rec)
}
