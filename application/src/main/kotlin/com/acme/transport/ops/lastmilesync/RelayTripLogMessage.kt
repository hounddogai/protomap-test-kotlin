package com.acme.transport.ops.lastmilesync

import com.acme.localization.v1.TranslationKeyCalculationServiceGrpcKt
import com.acme.localization.v1.quoteTranslationKeyRequest
import com.acme.modelregistry.v1.CountRegisteredModelsRequestKt
import com.acme.modelregistry.v1.RegisteredModelQueryServiceGrpcKt
import com.acme.modelregistry.v1.countRegisteredModelsRequest
import com.acme.telematics.v1.TelemetryReadingSyncServiceGrpcKt
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.getTelemetryReadingSyncStateRequest
import com.acme.telematics.v1.previewTripLogRequest
import com.acme.tours.v1.TourServiceGrpcKt
import com.acme.tours.v1.getTourRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TripLogValidationService/PreviewTripLog and hands what it keeps to the sink of its kind. */
suspend fun relayTripLogMessage(channel: ManagedChannel) {
    val reader = TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { reader.previewTripLog(previewTripLogRequest { this.tripLogId = "live-relay-trip-log-message" }) }
    val record = RelayTripLogMessageRecord(
        message = normalize(got.result.message),
        outcomeMessage = normalize(got.outcome.message),
        referenceId = got.outcome.referenceId,
    )
    val sinks = listOf<RelayTripLogMessageWriter>(
        RelayTripLogMessageTranslationKeyCalculation(TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub(channel)),
        RelayTripLogMessageRegisteredModelQuery(RegisteredModelQueryServiceGrpcKt.RegisteredModelQueryServiceCoroutineStub(channel)),
        RelayTripLogMessageTour(TourServiceGrpcKt.TourServiceCoroutineStub(channel)),
        RelayTripLogMessageTelemetryReadingSync(TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub(channel)),
    )
    sinks[3].forward(record)
}

/** Where relayTripLogMessage hands each record. */
interface RelayTripLogMessageWriter {
    suspend fun forward(rec: RelayTripLogMessageRecord)
}

/** What relayTripLogMessage keeps of each PreviewTripLog response. */
data class RelayTripLogMessageRecord(val message: String, val outcomeMessage: String, val referenceId: String)

/** Sends records to acme.localization.v1.TranslationKeyCalculationService. */
class RelayTripLogMessageTranslationKeyCalculation(private val stub: TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub) : RelayTripLogMessageWriter {
    override suspend fun forward(rec: RelayTripLogMessageRecord) {
        stub.quoteTranslationKey(quoteTranslationKeyRequest { this.translationKeyId = rec.referenceId })
    }
}

/** Writes records to acme.modelregistry.v1.RegisteredModelQueryService. */
class RelayTripLogMessageRegisteredModelQuery(private val stub: RegisteredModelQueryServiceGrpcKt.RegisteredModelQueryServiceCoroutineStub) : RelayTripLogMessageWriter {
    override suspend fun forward(rec: RelayTripLogMessageRecord) {
        val request = countRegisteredModelsRequest {
            this.filter = CountRegisteredModelsRequestKt.filter { this.ownerId = rec.referenceId; this.region = rec.outcomeMessage; this.query = rec.message }
        }
        stub.countRegisteredModels(request)
    }
}

/** Passes records to acme.tours.v1.TourService. */
class RelayTripLogMessageTour(private val stub: TourServiceGrpcKt.TourServiceCoroutineStub) : RelayTripLogMessageWriter {
    override suspend fun forward(rec: RelayTripLogMessageRecord) {
        stub.getTour(getTourRequest { this.tourId = rec.referenceId })
    }
}

/** Hands records to acme.telematics.v1.TelemetryReadingSyncService. */
class RelayTripLogMessageTelemetryReadingSync(private val stub: TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub) : RelayTripLogMessageWriter {
    override suspend fun forward(rec: RelayTripLogMessageRecord) {
        stub.getTelemetryReadingSyncState(getTelemetryReadingSyncStateRequest { this.telemetryReadingId = rec.message })
    }
}
