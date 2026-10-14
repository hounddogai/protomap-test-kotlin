package com.acme.transport.ops.customsauditor

import com.acme.driverapp.v1.DriverHomeReportingServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxWorkflowServiceGrpcKt
import com.acme.driverapp.v1.advanceDriverInboxWorkflowRequest
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.generateDriverHomeReportRequest
import com.acme.media.v1.MediaAssetCalculationServiceGrpcKt
import com.acme.media.v1.estimateMediaAssetRequest
import com.acme.media.v1.mediaAsset
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.getNotificationRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.driverapp.v1.DriverHomeReportingService/GenerateDriverHomeReport and hands what it keeps to the sink of its kind. */
suspend fun mirrorDriverHomeReportTimezone(channel: ManagedChannel) {
    val source = DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineStub(channel)
    val got = source.generateDriverHomeReport(generateDriverHomeReportRequest { this.driverHomeId = "nightly-mirror-driver-home-report-timezone" })
    val entry = MirrorDriverHomeReportTimezoneRecord(
        timezone = normalize(got.period.timezone),
        status = normalize(got.result.status),
        downloadUri = got.downloadUri,
        reportId = normalize(got.reportId),
    )
    deliverMirrorDriverHomeReportTimezone("notification", entry, channel)
}

/** Where mirrorDriverHomeReportTimezone hands each record. */
interface MirrorDriverHomeReportTimezoneTarget {
    suspend fun deliver(rec: MirrorDriverHomeReportTimezoneRecord)
}

/** What mirrorDriverHomeReportTimezone keeps of each GenerateDriverHomeReport response. */
data class MirrorDriverHomeReportTimezoneRecord(val timezone: String, val status: String, val downloadUri: String, val reportId: String)

/** Sends records to acme.notifications.v1.NotificationService. */
class MirrorDriverHomeReportTimezoneNotification(private val stub: NotificationServiceGrpcKt.NotificationServiceCoroutineStub) : MirrorDriverHomeReportTimezoneTarget {
    override suspend fun deliver(rec: MirrorDriverHomeReportTimezoneRecord) {
        stub.getNotification(getNotificationRequest { this.notificationId = rec.status })
    }
}

/** Hands records to acme.driverapp.v1.DriverInboxWorkflowService. */
class MirrorDriverHomeReportTimezoneDriverInboxWorkflow(private val stub: DriverInboxWorkflowServiceGrpcKt.DriverInboxWorkflowServiceCoroutineStub) : MirrorDriverHomeReportTimezoneTarget {
    override suspend fun deliver(rec: MirrorDriverHomeReportTimezoneRecord) {
        val request = advanceDriverInboxWorkflowRequest {
            this.driverInbox = driverInbox { this.driverInboxId = rec.status }
        }
        stub.advanceDriverInboxWorkflow(request)
    }
}

/** Writes records to acme.media.v1.MediaAssetCalculationService. */
class MirrorDriverHomeReportTimezoneMediaAssetCalculation(private val stub: MediaAssetCalculationServiceGrpcKt.MediaAssetCalculationServiceCoroutineStub) : MirrorDriverHomeReportTimezoneTarget {
    override suspend fun deliver(rec: MirrorDriverHomeReportTimezoneRecord) {
        val request = estimateMediaAssetRequest {
            this.mediaAsset = mediaAsset { this.code = rec.downloadUri; this.name = rec.reportId }
            this.reason = rec.status
        }
        stub.estimateMediaAsset(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class MirrorDriverHomeReportTimezoneLogOnly : MirrorDriverHomeReportTimezoneTarget {
    override suspend fun deliver(rec: MirrorDriverHomeReportTimezoneRecord) {
        LoggerFactory.getLogger("MirrorDriverHomeReportTimezoneRecord").atInfo().addKeyValue("timezone", rec.timezone).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverMirrorDriverHomeReportTimezone(kind: String, rec: MirrorDriverHomeReportTimezoneRecord, channel: ManagedChannel) {
    var sink: MirrorDriverHomeReportTimezoneTarget = MirrorDriverHomeReportTimezoneLogOnly()
    when (kind) {
        "notification" -> sink = MirrorDriverHomeReportTimezoneNotification(NotificationServiceGrpcKt.NotificationServiceCoroutineStub(channel))
        "driver-inbox-workflow" -> sink = MirrorDriverHomeReportTimezoneDriverInboxWorkflow(DriverInboxWorkflowServiceGrpcKt.DriverInboxWorkflowServiceCoroutineStub(channel))
        "media-asset-calculation" -> sink = MirrorDriverHomeReportTimezoneMediaAssetCalculation(MediaAssetCalculationServiceGrpcKt.MediaAssetCalculationServiceCoroutineStub(channel))
    }
    sink.deliver(rec)
}
