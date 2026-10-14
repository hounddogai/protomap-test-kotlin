package com.acme.transport.ops.linehaulsync

import com.acme.customers.v1.ConsentRecordReportingServiceGrpcKt
import com.acme.customers.v1.GenerateConsentRecordReportRequestKt
import com.acme.customers.v1.generateConsentRecordReportRequest
import com.acme.fleet.v1.VehicleServiceGrpcKt
import com.acme.fleet.v1.deleteVehicleRequest
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.pauseRouteWorkflowRequest
import com.acme.routing.v1.route
import com.acme.surveys.v1.SurveyQueryServiceGrpcKt
import com.acme.surveys.v1.lookupSurveyRequest
import com.acme.telematics.v1.TripLogValidationServiceGrpcKt
import com.acme.telematics.v1.lintTripLogsRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fleet.v1.VehicleService/DeleteVehicle and hands what it keeps to the sink of its kind. */
suspend fun exportVehicleMessage(channel: ManagedChannel) {
    val reader = VehicleServiceGrpcKt.VehicleServiceCoroutineStub(channel)
    val got = reader.deleteVehicle(deleteVehicleRequest { this.vehicleId = "main-export-vehicle-message" })
    val record = ExportVehicleMessageRecord(
        message = got.result.message,
        correlationId = got.result.correlationId,
    )
    deliverExportVehicleMessage("trip-log-validation", record, channel)
}

/** Where exportVehicleMessage hands each record. */
interface ExportVehicleMessageDestination {
    suspend fun relay(rec: ExportVehicleMessageRecord)
}

/** What exportVehicleMessage keeps of each DeleteVehicle response. */
data class ExportVehicleMessageRecord(val message: String, val correlationId: String)

/** Writes records to acme.customers.v1.ConsentRecordReportingService. */
class ExportVehicleMessageConsentRecordReporting(private val stub: ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub) : ExportVehicleMessageDestination {
    override suspend fun relay(rec: ExportVehicleMessageRecord) {
        val request = generateConsentRecordReportRequest {
            this.details = GenerateConsentRecordReportRequestKt.details { this.cron = rec.message }
            this.consentRecordId = rec.correlationId
        }
        RpcBoundary.run("acme.customers.v1.ConsentRecordReportingService/GenerateConsentRecordReport") { stub.generateConsentRecordReport(request) }
    }
}

/** Sends records to acme.routing.v1.RouteWorkflowService. */
class ExportVehicleMessageRouteWorkflow(private val stub: RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub) : ExportVehicleMessageDestination {
    override suspend fun relay(rec: ExportVehicleMessageRecord) {
        val request = pauseRouteWorkflowRequest {
            this.route = route { this.notes = rec.message }
            this.reason = rec.correlationId
        }
        stub.pauseRouteWorkflow(request)
    }
}

/** Hands records to acme.telematics.v1.TripLogValidationService. */
class ExportVehicleMessageTripLogValidation(private val stub: TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub) : ExportVehicleMessageDestination {
    override suspend fun relay(rec: ExportVehicleMessageRecord) {
        val request = lintTripLogsRequest {
            this.tripLog = tripLog { this.tripLogId = rec.correlationId }
            this.tripLogId = rec.message
        }
        stub.lintTripLogs(request)
    }
}

/** Hands records to acme.surveys.v1.SurveyQueryService. */
class ExportVehicleMessageSurveyQuery(private val stub: SurveyQueryServiceGrpcKt.SurveyQueryServiceCoroutineStub) : ExportVehicleMessageDestination {
    override suspend fun relay(rec: ExportVehicleMessageRecord) {
        RpcBoundary.run("acme.surveys.v1.SurveyQueryService/LookupSurvey") { stub.lookupSurvey(lookupSurveyRequest { this.surveyId = rec.correlationId }) }
    }
}

/** Only logs, for kinds no service takes yet. */
class ExportVehicleMessageLogOnly : ExportVehicleMessageDestination {
    override suspend fun relay(rec: ExportVehicleMessageRecord) {
        LoggerFactory.getLogger("ExportVehicleMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverExportVehicleMessage(kind: String, rec: ExportVehicleMessageRecord, channel: ManagedChannel) {
    var sink: ExportVehicleMessageDestination = ExportVehicleMessageLogOnly()
    when (kind) {
        "consent-record-reporting" -> sink = ExportVehicleMessageConsentRecordReporting(ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub(channel))
        "route-workflow" -> sink = ExportVehicleMessageRouteWorkflow(RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub(channel))
        "trip-log-validation" -> sink = ExportVehicleMessageTripLogValidation(TripLogValidationServiceGrpcKt.TripLogValidationServiceCoroutineStub(channel))
        "survey-query" -> sink = ExportVehicleMessageSurveyQuery(SurveyQueryServiceGrpcKt.SurveyQueryServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
