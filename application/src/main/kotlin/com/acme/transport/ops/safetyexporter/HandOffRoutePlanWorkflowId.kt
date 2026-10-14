package com.acme.transport.ops.safetyexporter

import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.recalculateZonesRequest
import com.acme.dispatch.v1.zone
import com.acme.patientapp.v1.PatientHomeQueryServiceGrpcKt
import com.acme.patientapp.v1.SuggestPatientHomesRequestKt
import com.acme.patientapp.v1.suggestPatientHomesRequest
import com.acme.performers.v1.PerformerServiceGrpcKt
import com.acme.performers.v1.createPerformerRequest
import com.acme.performers.v1.performer
import com.acme.routing.v1.RoutePlanWorkflowServiceGrpcKt
import com.acme.routing.v1.pauseRoutePlanWorkflowRequest
import com.acme.telematics.v1.TripLogExportServiceGrpcKt
import com.acme.telematics.v1.cancelTripLogExportRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.routing.v1.RoutePlanWorkflowService/PauseRoutePlanWorkflow and hands what it keeps to the sink of its kind. */
suspend fun handOffRoutePlanWorkflowId(channel: ManagedChannel) {
    val source = RoutePlanWorkflowServiceGrpcKt.RoutePlanWorkflowServiceCoroutineStub(channel)
    val got = source.pauseRoutePlanWorkflow(pauseRoutePlanWorkflowRequest { this.reason = "main-hand-off-route-plan-workflow-id" })
    val row = HandOffRoutePlanWorkflowIdRecord(
        referenceId = normalize(got.outcome.referenceId),
        message = normalize(got.result.message),
    )
    val sinks = listOf<HandOffRoutePlanWorkflowIdTarget>(
        HandOffRoutePlanWorkflowIdTripLogExport(TripLogExportServiceGrpcKt.TripLogExportServiceCoroutineStub(channel)),
        HandOffRoutePlanWorkflowIdZoneCalculation(ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub(channel)),
        HandOffRoutePlanWorkflowIdPatientHomeQuery(PatientHomeQueryServiceGrpcKt.PatientHomeQueryServiceCoroutineStub(channel)),
        HandOffRoutePlanWorkflowIdPerformer(PerformerServiceGrpcKt.PerformerServiceCoroutineStub(channel)),
    )
    sinks[0].deliver(row)
}

/** Where handOffRoutePlanWorkflowId hands each record. */
interface HandOffRoutePlanWorkflowIdTarget {
    suspend fun deliver(rec: HandOffRoutePlanWorkflowIdRecord)
}

/** What handOffRoutePlanWorkflowId keeps of each PauseRoutePlanWorkflow response. */
data class HandOffRoutePlanWorkflowIdRecord(val referenceId: String, val message: String)

/** Writes records to acme.telematics.v1.TripLogExportService. */
class HandOffRoutePlanWorkflowIdTripLogExport(private val stub: TripLogExportServiceGrpcKt.TripLogExportServiceCoroutineStub) : HandOffRoutePlanWorkflowIdTarget {
    override suspend fun deliver(rec: HandOffRoutePlanWorkflowIdRecord) {
        RpcBoundary.run("acme.telematics.v1.TripLogExportService/CancelTripLogExport") { stub.cancelTripLogExport(cancelTripLogExportRequest { this.tripLogId = rec.referenceId }) }
    }
}

/** Writes records to acme.dispatch.v1.ZoneCalculationService. */
class HandOffRoutePlanWorkflowIdZoneCalculation(private val stub: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub) : HandOffRoutePlanWorkflowIdTarget {
    override suspend fun deliver(rec: HandOffRoutePlanWorkflowIdRecord) {
        stub.recalculateZones(recalculateZonesRequest { this.zone = zone { this.zoneId = rec.message } })
    }
}

/** Passes records to acme.patientapp.v1.PatientHomeQueryService. */
class HandOffRoutePlanWorkflowIdPatientHomeQuery(private val stub: PatientHomeQueryServiceGrpcKt.PatientHomeQueryServiceCoroutineStub) : HandOffRoutePlanWorkflowIdTarget {
    override suspend fun deliver(rec: HandOffRoutePlanWorkflowIdRecord) {
        val request = suggestPatientHomesRequest {
            this.filter = SuggestPatientHomesRequestKt.filter { this.query = rec.referenceId }
            this.orderBy = rec.message
        }
        stub.suggestPatientHomes(request)
    }
}

/** Passes records to acme.performers.v1.PerformerService. */
class HandOffRoutePlanWorkflowIdPerformer(private val stub: PerformerServiceGrpcKt.PerformerServiceCoroutineStub) : HandOffRoutePlanWorkflowIdTarget {
    override suspend fun deliver(rec: HandOffRoutePlanWorkflowIdRecord) {
        val request = createPerformerRequest {
            this.requestId = rec.message
            this.performer = performer { this.performerId = rec.referenceId }
        }
        stub.createPerformer(request)
    }
}
