package com.acme.transport.ops.linehaulsync

import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.browseDriversRequest
import com.acme.fuel.v1.CommentFuelPolicyApprovalRequestKt
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.commentFuelPolicyApprovalRequest
import com.acme.telematics.v1.ListTelemetryReadingReportsRequestKt
import com.acme.telematics.v1.TelemetryReadingReportingServiceGrpcKt
import com.acme.telematics.v1.listTelemetryReadingReportsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.drivers.v1.DriverQueryService/BrowseDrivers and hands what it keeps to the sink of its kind. */
suspend fun pushDriversMessage(channel: ManagedChannel) {
    val reader = DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub(channel)
    val found = RpcBoundary.read { reader.browseDrivers(browseDriversRequest { this.orderBy = "main-push-drivers-message" }) }
    val entry = PushDriversMessageRecord(
        message = found.result.message,
        nextPageToken = normalize(found.pageInfo.nextPageToken),
        status = found.result.status,
        correlationId = normalize(found.result.correlationId),
    )
    dispatchPushDriversMessage("telemetry-reading-reporting", entry, channel)
}

/** Where pushDriversMessage hands each record. */
interface PushDriversMessageTarget {
    suspend fun hand(rec: PushDriversMessageRecord)
}

/** What pushDriversMessage keeps of each BrowseDrivers response. */
data class PushDriversMessageRecord(val message: String, val nextPageToken: String, val status: String, val correlationId: String)

/** Hands records to acme.fuel.v1.FuelPolicyApprovalService. */
class PushDriversMessageFuelPolicyApproval(private val stub: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub) : PushDriversMessageTarget {
    override suspend fun hand(rec: PushDriversMessageRecord) {
        val request = commentFuelPolicyApprovalRequest {
            this.details = CommentFuelPolicyApprovalRequestKt.details { this.comment = rec.message }
        }
        stub.commentFuelPolicyApproval(request)
    }
}

/** Hands records to acme.telematics.v1.TelemetryReadingReportingService. */
class PushDriversMessageTelemetryReadingReporting(private val stub: TelemetryReadingReportingServiceGrpcKt.TelemetryReadingReportingServiceCoroutineStub) : PushDriversMessageTarget {
    override suspend fun hand(rec: PushDriversMessageRecord) {
        val request = listTelemetryReadingReportsRequest {
            this.orderBy = rec.correlationId
            this.filter = ListTelemetryReadingReportsRequestKt.filter { this.ownerId = rec.message }
        }
        stub.listTelemetryReadingReports(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class PushDriversMessageLogOnly : PushDriversMessageTarget {
    override suspend fun hand(rec: PushDriversMessageRecord) {
        LoggerFactory.getLogger("PushDriversMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchPushDriversMessage(kind: String, rec: PushDriversMessageRecord, channel: ManagedChannel) {
    var write: suspend (PushDriversMessageRecord) -> Unit = PushDriversMessageLogOnly()::hand
    when (kind) {
        "fuel-policy-approval" -> write = PushDriversMessageFuelPolicyApproval(FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub(channel))::hand
        "telemetry-reading-reporting" -> write = PushDriversMessageTelemetryReadingReporting(TelemetryReadingReportingServiceGrpcKt.TelemetryReadingReportingServiceCoroutineStub(channel))::hand
    }
    write(rec)
}
