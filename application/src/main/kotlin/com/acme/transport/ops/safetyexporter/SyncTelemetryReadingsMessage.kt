package com.acme.transport.ops.safetyexporter

import com.acme.maintenance.v1.ExplainPartDecisionRequestKt
import com.acme.maintenance.v1.PartValidationServiceGrpcKt
import com.acme.maintenance.v1.explainPartDecisionRequest
import com.acme.maintenance.v1.part
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.resolveTelemetryReadingsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TelemetryReadingQueryService/ResolveTelemetryReadings and hands what it keeps to acme.maintenance.v1.PartValidationService. */
suspend fun syncTelemetryReadingsMessage(channel: ManagedChannel) {
    val reader = TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub(channel)
    val found = reader.resolveTelemetryReadings(resolveTelemetryReadingsRequest { })
    val entry = SyncTelemetryReadingsMessageRecord(
        message = found.result.message,
        nextPageToken = found.pageInfo.nextPageToken,
        status = found.result.status,
        correlationId = found.result.correlationId,
    )
    SyncTelemetryReadingsMessagePartValidation(PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub(channel)).write(entry)
}

/** What syncTelemetryReadingsMessage keeps of each ResolveTelemetryReadings response. */
data class SyncTelemetryReadingsMessageRecord(val message: String, val nextPageToken: String, val status: String, val correlationId: String)

/** Sends records to acme.maintenance.v1.PartValidationService. */
class SyncTelemetryReadingsMessagePartValidation(private val stub: PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub) {
    suspend fun write(rec: SyncTelemetryReadingsMessageRecord) {
        val request = explainPartDecisionRequest {
            this.reason = rec.nextPageToken
            this.details = ExplainPartDecisionRequestKt.details { this.locale = rec.status }
            this.part = part { this.partId = rec.correlationId }
        }
        RpcBoundary.run("acme.maintenance.v1.PartValidationService/ExplainPartDecision") { stub.explainPartDecision(request) }
    }
}
