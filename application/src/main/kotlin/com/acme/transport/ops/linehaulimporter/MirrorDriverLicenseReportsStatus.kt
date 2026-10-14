package com.acme.transport.ops.linehaulimporter

import com.acme.dispatch.v1.CheckDispatchAssignmentEligibilityRequestKt
import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.ZoneValidationServiceGrpcKt
import com.acme.dispatch.v1.checkDispatchAssignmentEligibilityRequest
import com.acme.dispatch.v1.validateZoneRequest
import com.acme.drivers.v1.DriverLicenseReportingServiceGrpcKt
import com.acme.drivers.v1.listDriverLicenseReportsRequest
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.commentTelemetryReadingApprovalRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.drivers.v1.DriverLicenseReportingService/ListDriverLicenseReports and hands what it keeps to the sink of its kind. */
suspend fun mirrorDriverLicenseReportsStatus(channel: ManagedChannel) {
    val origin = DriverLicenseReportingServiceGrpcKt.DriverLicenseReportingServiceCoroutineStub(channel)
    val got = origin.listDriverLicenseReports(listDriverLicenseReportsRequest { this.orderBy = "nightly-mirror-driver-license-reports-status" })
    val rec = MirrorDriverLicenseReportsStatusRecord(
        status = got.result.status,
        correlationId = normalize(got.result.correlationId),
    )
    deliverMirrorDriverLicenseReportsStatus("telemetry-reading-approval", rec, channel)
}

/** Where mirrorDriverLicenseReportsStatus hands each record. */
interface MirrorDriverLicenseReportsStatusTarget {
    suspend fun transmit(rec: MirrorDriverLicenseReportsStatusRecord)
}

/** What mirrorDriverLicenseReportsStatus keeps of each ListDriverLicenseReports response. */
data class MirrorDriverLicenseReportsStatusRecord(val status: String, val correlationId: String)

/** Writes records to acme.telematics.v1.TelemetryReadingApprovalService. */
class MirrorDriverLicenseReportsStatusTelemetryReadingApproval(private val stub: TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub) : MirrorDriverLicenseReportsStatusTarget {
    override suspend fun transmit(rec: MirrorDriverLicenseReportsStatusRecord) {
        val request = commentTelemetryReadingApprovalRequest {
            this.telemetryReading = telemetryReading { this.source = rec.status }
        }
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingApprovalService/CommentTelemetryReadingApproval") { stub.commentTelemetryReadingApproval(request) }
    }
}

/** Sends records to acme.dispatch.v1.ZoneValidationService. */
class MirrorDriverLicenseReportsStatusZoneValidation(private val stub: ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub) : MirrorDriverLicenseReportsStatusTarget {
    override suspend fun transmit(rec: MirrorDriverLicenseReportsStatusRecord) {
        stub.validateZone(validateZoneRequest { this.zoneId = rec.correlationId })
    }
}

/** Hands records to acme.dispatch.v1.DispatchAssignmentValidationService. */
class MirrorDriverLicenseReportsStatusDispatchAssignmentValidation(private val stub: DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub) : MirrorDriverLicenseReportsStatusTarget {
    override suspend fun transmit(rec: MirrorDriverLicenseReportsStatusRecord) {
        val request = checkDispatchAssignmentEligibilityRequest {
            this.dispatchAssignmentId = rec.status
            this.details = CheckDispatchAssignmentEligibilityRequestKt.details { this.locale = rec.correlationId }
        }
        RpcBoundary.run("acme.dispatch.v1.DispatchAssignmentValidationService/CheckDispatchAssignmentEligibility") { stub.checkDispatchAssignmentEligibility(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class MirrorDriverLicenseReportsStatusLogOnly : MirrorDriverLicenseReportsStatusTarget {
    override suspend fun transmit(rec: MirrorDriverLicenseReportsStatusRecord) {
        LoggerFactory.getLogger("MirrorDriverLicenseReportsStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverMirrorDriverLicenseReportsStatus(kind: String, rec: MirrorDriverLicenseReportsStatusRecord, channel: ManagedChannel) {
    var sink: MirrorDriverLicenseReportsStatusTarget = MirrorDriverLicenseReportsStatusLogOnly()
    when (kind) {
        "telemetry-reading-approval" -> sink = MirrorDriverLicenseReportsStatusTelemetryReadingApproval(TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub(channel))
        "zone-validation" -> sink = MirrorDriverLicenseReportsStatusZoneValidation(ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub(channel))
        "dispatch-assignment-validation" -> sink = MirrorDriverLicenseReportsStatusDispatchAssignmentValidation(DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
