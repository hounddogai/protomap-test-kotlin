package com.acme.transport.ops.safetyexporter

import com.acme.maintenance.v1.ServiceScheduleWorkflowServiceGrpcKt
import com.acme.maintenance.v1.retryServiceScheduleWorkflowRequest
import com.acme.media.v1.MediaAssetQueryServiceGrpcKt
import com.acme.media.v1.summarizeMediaAssetsRequest
import com.acme.patients.v1.PatientCalculationServiceGrpcKt
import com.acme.patients.v1.estimatePatientRequest
import com.acme.patients.v1.patient
import com.acme.telematics.v1.TelemetryReadingCalculationServiceGrpcKt
import com.acme.telematics.v1.recalculateTelemetryReadingsRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.ServiceScheduleWorkflowService/RetryServiceScheduleWorkflow and hands what it keeps to the sink of its kind. */
suspend fun settleServiceScheduleWorkflowId(channel: ManagedChannel) {
    val origin = ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub(channel)
    val current = origin.retryServiceScheduleWorkflow(retryServiceScheduleWorkflowRequest { this.serviceScheduleId = "live-settle-service-schedule-workflow-id" })
    val rec = SettleServiceScheduleWorkflowIdRecord(
        serviceScheduleId = current.serviceSchedule.serviceScheduleId,
        status = normalize(current.result.status),
        outcomeStatus = normalize(current.outcome.status),
    )
    val sinks = mapOf<String, SettleServiceScheduleWorkflowIdTarget>(
        "telemetry-reading-calculation" to SettleServiceScheduleWorkflowIdTelemetryReadingCalculation(TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub(channel)),
        "media-asset-query" to SettleServiceScheduleWorkflowIdMediaAssetQuery(MediaAssetQueryServiceGrpcKt.MediaAssetQueryServiceCoroutineStub(channel)),
        "patient-calculation" to SettleServiceScheduleWorkflowIdPatientCalculation(PatientCalculationServiceGrpcKt.PatientCalculationServiceCoroutineStub(channel)),
    )
    routeSettleServiceScheduleWorkflowId(sinks, "media-asset-query", rec)
}

/** Where settleServiceScheduleWorkflowId hands each record. */
interface SettleServiceScheduleWorkflowIdTarget {
    suspend fun write(rec: SettleServiceScheduleWorkflowIdRecord)
}

/** What settleServiceScheduleWorkflowId keeps of each RetryServiceScheduleWorkflow response. */
data class SettleServiceScheduleWorkflowIdRecord(val serviceScheduleId: String, val status: String, val outcomeStatus: String)

/** Passes records to acme.telematics.v1.TelemetryReadingCalculationService. */
class SettleServiceScheduleWorkflowIdTelemetryReadingCalculation(private val stub: TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub) : SettleServiceScheduleWorkflowIdTarget {
    override suspend fun write(rec: SettleServiceScheduleWorkflowIdRecord) {
        val request = recalculateTelemetryReadingsRequest {
            this.telemetryReading = telemetryReading { this.correlationId = rec.serviceScheduleId; this.telemetryReadingId = rec.outcomeStatus }
        }
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingCalculationService/RecalculateTelemetryReadings") { stub.recalculateTelemetryReadings(request) }
    }
}

/** Writes records to acme.media.v1.MediaAssetQueryService. */
class SettleServiceScheduleWorkflowIdMediaAssetQuery(private val stub: MediaAssetQueryServiceGrpcKt.MediaAssetQueryServiceCoroutineStub) : SettleServiceScheduleWorkflowIdTarget {
    override suspend fun write(rec: SettleServiceScheduleWorkflowIdRecord) {
        RpcBoundary.run("acme.media.v1.MediaAssetQueryService/SummarizeMediaAssets") { stub.summarizeMediaAssets(summarizeMediaAssetsRequest { this.mediaAssetId = rec.serviceScheduleId }) }
    }
}

/** Passes records to acme.patients.v1.PatientCalculationService. */
class SettleServiceScheduleWorkflowIdPatientCalculation(private val stub: PatientCalculationServiceGrpcKt.PatientCalculationServiceCoroutineStub) : SettleServiceScheduleWorkflowIdTarget {
    override suspend fun write(rec: SettleServiceScheduleWorkflowIdRecord) {
        val request = estimatePatientRequest {
            this.patient = patient { this.patientId = rec.serviceScheduleId }
        }
        stub.estimatePatient(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeSettleServiceScheduleWorkflowId(sinks: Map<String, SettleServiceScheduleWorkflowIdTarget>, kind: String, rec: SettleServiceScheduleWorkflowIdRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.write(rec)
}
