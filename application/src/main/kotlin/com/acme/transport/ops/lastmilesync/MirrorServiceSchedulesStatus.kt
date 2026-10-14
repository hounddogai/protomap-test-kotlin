package com.acme.transport.ops.lastmilesync

import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.SuspendWorkOrderRequestKt
import com.acme.maintenance.v1.WorkOrderLifecycleServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.recalculateServiceSchedulesRequest
import com.acme.maintenance.v1.signalWorkOrderWorkflowRequest
import com.acme.maintenance.v1.suspendWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.telematics.v1.TelemetryReadingReportingServiceGrpcKt
import com.acme.telematics.v1.generateTelemetryReadingReportRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.ServiceScheduleCalculationService/RecalculateServiceSchedules and hands what it keeps to the sink of its kind. */
suspend fun mirrorServiceSchedulesStatus(channel: ManagedChannel) {
    val source = ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub(channel)
    val reply = source.recalculateServiceSchedules(recalculateServiceSchedulesRequest { this.reason = "current-mirror-service-schedules-status" })
    val entry = MirrorServiceSchedulesStatusRecord(
        status = normalize(reply.result.status),
        correlationId = reply.result.correlationId,
        title = reply.serviceSchedule.title,
    )
    val sinks = mapOf<String, MirrorServiceSchedulesStatusDestination>(
        "work-order-lifecycle" to MirrorServiceSchedulesStatusWorkOrderLifecycle(WorkOrderLifecycleServiceGrpcKt.WorkOrderLifecycleServiceCoroutineStub(channel)),
        "work-order-workflow" to MirrorServiceSchedulesStatusWorkOrderWorkflow(WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub(channel)),
        "telemetry-reading-reporting" to MirrorServiceSchedulesStatusTelemetryReadingReporting(TelemetryReadingReportingServiceGrpcKt.TelemetryReadingReportingServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("MIRROR_SERVICE_SCHEDULES_STATUS_TARGET")] ?: return
    sink.hand(entry)
}

/** Where mirrorServiceSchedulesStatus hands each record. */
interface MirrorServiceSchedulesStatusDestination {
    suspend fun hand(rec: MirrorServiceSchedulesStatusRecord)
}

/** What mirrorServiceSchedulesStatus keeps of each RecalculateServiceSchedules response. */
data class MirrorServiceSchedulesStatusRecord(val status: String, val correlationId: String, val title: String)

/** Hands records to acme.maintenance.v1.WorkOrderLifecycleService. */
class MirrorServiceSchedulesStatusWorkOrderLifecycle(private val stub: WorkOrderLifecycleServiceGrpcKt.WorkOrderLifecycleServiceCoroutineStub) : MirrorServiceSchedulesStatusDestination {
    override suspend fun hand(rec: MirrorServiceSchedulesStatusRecord) {
        val request = suspendWorkOrderRequest {
            this.workOrderId = rec.status
            this.details = SuspendWorkOrderRequestKt.details { this.reasonCode = rec.correlationId }
            this.reason = rec.title
        }
        stub.suspendWorkOrder(request)
    }
}

/** Passes records to acme.maintenance.v1.WorkOrderWorkflowService. */
class MirrorServiceSchedulesStatusWorkOrderWorkflow(private val stub: WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub) : MirrorServiceSchedulesStatusDestination {
    override suspend fun hand(rec: MirrorServiceSchedulesStatusRecord) {
        val request = signalWorkOrderWorkflowRequest {
            this.reason = rec.status
            this.workOrder = workOrder { this.subject = rec.title }
        }
        stub.signalWorkOrderWorkflow(request)
    }
}

/** Writes records to acme.telematics.v1.TelemetryReadingReportingService. */
class MirrorServiceSchedulesStatusTelemetryReadingReporting(private val stub: TelemetryReadingReportingServiceGrpcKt.TelemetryReadingReportingServiceCoroutineStub) : MirrorServiceSchedulesStatusDestination {
    override suspend fun hand(rec: MirrorServiceSchedulesStatusRecord) {
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingReportingService/GenerateTelemetryReadingReport") { stub.generateTelemetryReadingReport(generateTelemetryReadingReportRequest { this.reason = rec.title }) }
    }
}
