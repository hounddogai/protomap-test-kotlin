package com.acme.transport.ops.lastmilesync

import com.acme.dispatch.v1.DispatchAssignmentWorkflowServiceGrpcKt
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.resumeDispatchAssignmentWorkflowRequest
import com.acme.fleet.v1.InspectionLifecycleServiceGrpcKt
import com.acme.fleet.v1.ScheduleInspectionExpiryRequestKt
import com.acme.fleet.v1.scheduleInspectionExpiryRequest
import com.acme.seating.v1.SeatMapServiceGrpcKt
import com.acme.seating.v1.getSeatMapRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.seating.v1.SeatMapService/GetSeatMap and hands what it keeps to the sink of its kind. */
suspend fun relaySeatMapId(channel: ManagedChannel) {
    val origin = SeatMapServiceGrpcKt.SeatMapServiceCoroutineStub(channel)
    val found = origin.getSeatMap(getSeatMapRequest { this.seatMapId = "primary-relay-seat-map-id" })
    val record = RelaySeatMapIdRecord(
        correlationId = found.result.correlationId,
        parentId = found.seatMap.parentId,
    )
    dispatchRelaySeatMapId("inspection-lifecycle", record, channel)
}

/** Where relaySeatMapId hands each record. */
interface RelaySeatMapIdWriter {
    suspend fun forward(rec: RelaySeatMapIdRecord)
}

/** What relaySeatMapId keeps of each GetSeatMap response. */
data class RelaySeatMapIdRecord(val correlationId: String, val parentId: String)

/** Passes records to acme.dispatch.v1.DispatchAssignmentWorkflowService. */
class RelaySeatMapIdDispatchAssignmentWorkflow(private val stub: DispatchAssignmentWorkflowServiceGrpcKt.DispatchAssignmentWorkflowServiceCoroutineStub) : RelaySeatMapIdWriter {
    override suspend fun forward(rec: RelaySeatMapIdRecord) {
        val request = resumeDispatchAssignmentWorkflowRequest {
            this.dispatchAssignment = dispatchAssignment { this.subject = rec.correlationId }
        }
        stub.resumeDispatchAssignmentWorkflow(request)
    }
}

/** Sends records to acme.fleet.v1.InspectionLifecycleService. */
class RelaySeatMapIdInspectionLifecycle(private val stub: InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub) : RelaySeatMapIdWriter {
    override suspend fun forward(rec: RelaySeatMapIdRecord) {
        val request = scheduleInspectionExpiryRequest {
            this.details = ScheduleInspectionExpiryRequestKt.details { this.step = rec.correlationId }
        }
        RpcBoundary.run("acme.fleet.v1.InspectionLifecycleService/ScheduleInspectionExpiry") { stub.scheduleInspectionExpiry(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class RelaySeatMapIdLogOnly : RelaySeatMapIdWriter {
    override suspend fun forward(rec: RelaySeatMapIdRecord) {
        LoggerFactory.getLogger("RelaySeatMapIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchRelaySeatMapId(kind: String, rec: RelaySeatMapIdRecord, channel: ManagedChannel) {
    var write: suspend (RelaySeatMapIdRecord) -> Unit = RelaySeatMapIdLogOnly()::forward
    when (kind) {
        "dispatch-assignment-workflow" -> write = RelaySeatMapIdDispatchAssignmentWorkflow(DispatchAssignmentWorkflowServiceGrpcKt.DispatchAssignmentWorkflowServiceCoroutineStub(channel))::forward
        "inspection-lifecycle" -> write = RelaySeatMapIdInspectionLifecycle(InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub(channel))::forward
    }
    write(rec)
}
