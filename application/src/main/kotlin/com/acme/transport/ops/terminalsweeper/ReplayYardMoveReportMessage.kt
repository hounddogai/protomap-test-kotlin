package com.acme.transport.ops.terminalsweeper

import com.acme.common.v1.pageRequest
import com.acme.drivers.v1.DriverWorkflowServiceGrpcKt
import com.acme.drivers.v1.listDriverWorkflowsRequest
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.explainInspectionCalculationRequest
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.simulateRoutePlanRequest
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.scheduleYardMoveReportRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.YardMoveReportingService/ScheduleYardMoveReport and hands what it keeps to the sink of its kind. */
suspend fun replayYardMoveReportMessage(channel: ManagedChannel) {
    val upstream = YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub(channel)
    val reply = upstream.scheduleYardMoveReport(scheduleYardMoveReportRequest { this.yardMoveId = "default-replay-yard-move-report-message" })
    val rec = ReplayYardMoveReportMessageRecord(
        message = reply.result.message,
        timezone = reply.period.timezone,
    )
    val sinks = listOf<ReplayYardMoveReportMessageSink>(
        ReplayYardMoveReportMessageRoutePlanCalculation(RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub(channel)),
        ReplayYardMoveReportMessageInspectionCalculation(InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub(channel)),
        ReplayYardMoveReportMessageDriverWorkflow(DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub(channel)),
    )
    sinks[1].relay(rec)
}

/** Where replayYardMoveReportMessage hands each record. */
interface ReplayYardMoveReportMessageSink {
    suspend fun relay(rec: ReplayYardMoveReportMessageRecord)
}

/** What replayYardMoveReportMessage keeps of each ScheduleYardMoveReport response. */
data class ReplayYardMoveReportMessageRecord(val message: String, val timezone: String)

/** Passes records to acme.routing.v1.RoutePlanCalculationService. */
class ReplayYardMoveReportMessageRoutePlanCalculation(private val stub: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub) : ReplayYardMoveReportMessageSink {
    override suspend fun relay(rec: ReplayYardMoveReportMessageRecord) {
        stub.simulateRoutePlan(simulateRoutePlanRequest { this.reason = rec.timezone })
    }
}

/** Sends records to acme.fleet.v1.InspectionCalculationService. */
class ReplayYardMoveReportMessageInspectionCalculation(private val stub: InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub) : ReplayYardMoveReportMessageSink {
    override suspend fun relay(rec: ReplayYardMoveReportMessageRecord) {
        val request = explainInspectionCalculationRequest {
            this.reason = rec.timezone
            this.inspectionId = rec.message
        }
        stub.explainInspectionCalculation(request)
    }
}

/** Hands records to acme.drivers.v1.DriverWorkflowService. */
class ReplayYardMoveReportMessageDriverWorkflow(private val stub: DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub) : ReplayYardMoveReportMessageSink {
    override suspend fun relay(rec: ReplayYardMoveReportMessageRecord) {
        val request = listDriverWorkflowsRequest {
            this.page = pageRequest { this.pageToken = rec.timezone }
            this.orderBy = rec.message
        }
        stub.listDriverWorkflows(request)
    }
}
