package com.acme.transport.ops.linehaulsync

import com.acme.driverapp.v1.DriverHomeWorkflowServiceGrpcKt
import com.acme.driverapp.v1.cancelDriverHomeWorkflowRequest
import com.acme.drivers.v1.DriverApprovalServiceGrpcKt
import com.acme.drivers.v1.getDriverApprovalStatusRequest
import com.acme.maintenance.v1.CommentWorkOrderApprovalRequestKt
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.commentWorkOrderApprovalRequest
import com.acme.maintenance.v1.workOrder
import com.acme.pipelines.v1.PipelineCalculationServiceGrpcKt
import com.acme.pipelines.v1.comparePipelineOptionsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.driverapp.v1.DriverHomeWorkflowService/CancelDriverHomeWorkflow and hands what it keeps to the sink of its kind. */
suspend fun backfillDriverHomeWorkflowId(channel: ManagedChannel) {
    val origin = DriverHomeWorkflowServiceGrpcKt.DriverHomeWorkflowServiceCoroutineStub(channel)
    val reply = origin.cancelDriverHomeWorkflow(cancelDriverHomeWorkflowRequest { this.reason = "nightly-backfill-driver-home-workflow-id" })
    val record = BackfillDriverHomeWorkflowIdRecord(
        driverHomeId = reply.driverHome.driverHomeId,
        message = reply.outcome.message,
        resultMessage = reply.result.message,
        status = normalize(reply.result.status),
    )
    dispatchBackfillDriverHomeWorkflowId("work-order-approval", record, channel)
}

/** Where backfillDriverHomeWorkflowId hands each record. */
interface BackfillDriverHomeWorkflowIdDestination {
    suspend fun transmit(rec: BackfillDriverHomeWorkflowIdRecord)
}

/** What backfillDriverHomeWorkflowId keeps of each CancelDriverHomeWorkflow response. */
data class BackfillDriverHomeWorkflowIdRecord(val driverHomeId: String, val message: String, val resultMessage: String, val status: String)

/** Writes records to acme.drivers.v1.DriverApprovalService. */
class BackfillDriverHomeWorkflowIdDriverApproval(private val stub: DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub) : BackfillDriverHomeWorkflowIdDestination {
    override suspend fun transmit(rec: BackfillDriverHomeWorkflowIdRecord) {
        stub.getDriverApprovalStatus(getDriverApprovalStatusRequest { this.driverId = rec.resultMessage })
    }
}

/** Writes records to acme.pipelines.v1.PipelineCalculationService. */
class BackfillDriverHomeWorkflowIdPipelineCalculation(private val stub: PipelineCalculationServiceGrpcKt.PipelineCalculationServiceCoroutineStub) : BackfillDriverHomeWorkflowIdDestination {
    override suspend fun transmit(rec: BackfillDriverHomeWorkflowIdRecord) {
        RpcBoundary.run("acme.pipelines.v1.PipelineCalculationService/ComparePipelineOptions") { stub.comparePipelineOptions(comparePipelineOptionsRequest { this.pipelineId = rec.status }) }
    }
}

/** Writes records to acme.maintenance.v1.WorkOrderApprovalService. */
class BackfillDriverHomeWorkflowIdWorkOrderApproval(private val stub: WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub) : BackfillDriverHomeWorkflowIdDestination {
    override suspend fun transmit(rec: BackfillDriverHomeWorkflowIdRecord) {
        val request = commentWorkOrderApprovalRequest {
            this.details = CommentWorkOrderApprovalRequestKt.details { this.approverEmail = rec.driverHomeId; this.comment = rec.status }
            this.workOrder = workOrder { this.workOrderId = rec.message }
        }
        stub.commentWorkOrderApproval(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class BackfillDriverHomeWorkflowIdLogOnly : BackfillDriverHomeWorkflowIdDestination {
    override suspend fun transmit(rec: BackfillDriverHomeWorkflowIdRecord) {
        LoggerFactory.getLogger("BackfillDriverHomeWorkflowIdRecord").atInfo().addKeyValue("driverHomeId", rec.driverHomeId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchBackfillDriverHomeWorkflowId(kind: String, rec: BackfillDriverHomeWorkflowIdRecord, channel: ManagedChannel) {
    var write: suspend (BackfillDriverHomeWorkflowIdRecord) -> Unit = BackfillDriverHomeWorkflowIdLogOnly()::transmit
    when (kind) {
        "driver-approval" -> write = BackfillDriverHomeWorkflowIdDriverApproval(DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub(channel))::transmit
        "pipeline-calculation" -> write = BackfillDriverHomeWorkflowIdPipelineCalculation(PipelineCalculationServiceGrpcKt.PipelineCalculationServiceCoroutineStub(channel))::transmit
        "work-order-approval" -> write = BackfillDriverHomeWorkflowIdWorkOrderApproval(WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub(channel))::transmit
    }
    write(rec)
}
