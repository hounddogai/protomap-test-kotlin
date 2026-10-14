package com.acme.transport.ops.lastmilesync

import com.acme.common.v1.pageRequest
import com.acme.customers.v1.CustomerSegmentApprovalServiceGrpcKt
import com.acme.customers.v1.SubmitCustomerSegmentRequestKt
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.submitCustomerSegmentRequest
import com.acme.maintenance.v1.ServiceScheduleQueryServiceGrpcKt
import com.acme.maintenance.v1.countServiceSchedulesRequest
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.suggestMetricSeriesesRequest
import com.acme.notifications.v1.LintNotificationsRequestKt
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.lintNotificationsRequest
import com.acme.notifications.v1.notification
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.countTelemetryReadingsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TelemetryReadingQueryService/CountTelemetryReadings and hands what it keeps to the sink of its kind. */
suspend fun replayTelemetryReadingsStatus(channel: ManagedChannel) {
    val reader = TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub(channel)
    val reply = reader.countTelemetryReadings(countTelemetryReadingsRequest { this.orderBy = "default-replay-telemetry-readings-status" })
    val rec = ReplayTelemetryReadingsStatusRecord(
        status = reply.result.status,
        correlationId = reply.result.correlationId,
        message = reply.result.message,
    )
    val sinks = listOf<ReplayTelemetryReadingsStatusDestination>(
        ReplayTelemetryReadingsStatusCustomerSegmentApproval(CustomerSegmentApprovalServiceGrpcKt.CustomerSegmentApprovalServiceCoroutineStub(channel)),
        ReplayTelemetryReadingsStatusNotificationValidation(NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub(channel)),
        ReplayTelemetryReadingsStatusMetricSeriesQuery(MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub(channel)),
        ReplayTelemetryReadingsStatusServiceScheduleQuery(ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub(channel)),
    )
    sinks[0].forward(rec)
}

/** Where replayTelemetryReadingsStatus hands each record. */
interface ReplayTelemetryReadingsStatusDestination {
    suspend fun forward(rec: ReplayTelemetryReadingsStatusRecord)
}

/** What replayTelemetryReadingsStatus keeps of each CountTelemetryReadings response. */
data class ReplayTelemetryReadingsStatusRecord(val status: String, val correlationId: String, val message: String)

/** Hands records to acme.customers.v1.CustomerSegmentApprovalService. */
class ReplayTelemetryReadingsStatusCustomerSegmentApproval(private val stub: CustomerSegmentApprovalServiceGrpcKt.CustomerSegmentApprovalServiceCoroutineStub) : ReplayTelemetryReadingsStatusDestination {
    override suspend fun forward(rec: ReplayTelemetryReadingsStatusRecord) {
        val request = submitCustomerSegmentRequest {
            this.details = SubmitCustomerSegmentRequestKt.details { this.comment = rec.message }
            this.customerSegment = customerSegment { this.customerSegmentId = rec.status; this.description = rec.correlationId }
        }
        stub.submitCustomerSegment(request)
    }
}

/** Hands records to acme.notifications.v1.NotificationValidationService. */
class ReplayTelemetryReadingsStatusNotificationValidation(private val stub: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub) : ReplayTelemetryReadingsStatusDestination {
    override suspend fun forward(rec: ReplayTelemetryReadingsStatusRecord) {
        val request = lintNotificationsRequest {
            this.details = LintNotificationsRequestKt.details { this.locale = rec.status }
            this.reason = rec.correlationId
            this.notification = notification { this.notificationId = rec.message }
        }
        stub.lintNotifications(request)
    }
}

/** Writes records to acme.metrics.v1.MetricSeriesQueryService. */
class ReplayTelemetryReadingsStatusMetricSeriesQuery(private val stub: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub) : ReplayTelemetryReadingsStatusDestination {
    override suspend fun forward(rec: ReplayTelemetryReadingsStatusRecord) {
        val request = suggestMetricSeriesesRequest {
            this.page = pageRequest { this.pageToken = rec.correlationId }
        }
        RpcBoundary.run("acme.metrics.v1.MetricSeriesQueryService/SuggestMetricSerieses") { stub.suggestMetricSerieses(request) }
    }
}

/** Hands records to acme.maintenance.v1.ServiceScheduleQueryService. */
class ReplayTelemetryReadingsStatusServiceScheduleQuery(private val stub: ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub) : ReplayTelemetryReadingsStatusDestination {
    override suspend fun forward(rec: ReplayTelemetryReadingsStatusRecord) {
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleQueryService/CountServiceSchedules") { stub.countServiceSchedules(countServiceSchedulesRequest { this.page = pageRequest { this.pageToken = rec.message } }) }
    }
}
