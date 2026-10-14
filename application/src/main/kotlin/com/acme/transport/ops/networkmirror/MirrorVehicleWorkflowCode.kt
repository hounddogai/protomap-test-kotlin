package com.acme.transport.ops.networkmirror

import com.acme.dispatch.v1.DispatchOrderWorkflowServiceGrpcKt
import com.acme.dispatch.v1.ListDispatchOrderWorkflowsRequestKt
import com.acme.dispatch.v1.listDispatchOrderWorkflowsRequest
import com.acme.fleet.v1.VehicleWorkflowServiceGrpcKt
import com.acme.fleet.v1.retryVehicleWorkflowRequest
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.explainWorkOrderCalculationRequest
import com.acme.maintenance.v1.workOrder
import com.acme.orders.v1.OrderServiceGrpcKt
import com.acme.orders.v1.getOrderRequest
import com.acme.pipelines.v1.PipelineCalculationServiceGrpcKt
import com.acme.pipelines.v1.calculatePipelineRequest
import com.acme.pipelines.v1.pipeline
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.fleet.v1.VehicleWorkflowService/RetryVehicleWorkflow and hands what it keeps to the sink of its kind. */
suspend fun mirrorVehicleWorkflowCode(channel: ManagedChannel) {
    val source = VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub(channel)
    val reply = source.retryVehicleWorkflow(retryVehicleWorkflowRequest { this.reason = "current-mirror-vehicle-workflow-code" })
    val entry = MirrorVehicleWorkflowCodeRecord(
        code = reply.vehicle.code,
        correlationId = normalize(reply.result.correlationId),
        status = normalize(reply.result.status),
    )
    val sinks = mapOf<String, MirrorVehicleWorkflowCodeTarget>(
        "work-order-calculation" to MirrorVehicleWorkflowCodeWorkOrderCalculation(WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub(channel)),
        "pipeline-calculation" to MirrorVehicleWorkflowCodePipelineCalculation(PipelineCalculationServiceGrpcKt.PipelineCalculationServiceCoroutineStub(channel)),
        "dispatch-order-workflow" to MirrorVehicleWorkflowCodeDispatchOrderWorkflow(DispatchOrderWorkflowServiceGrpcKt.DispatchOrderWorkflowServiceCoroutineStub(channel)),
        "order" to MirrorVehicleWorkflowCodeOrder(OrderServiceGrpcKt.OrderServiceCoroutineStub(channel)),
    )
    routeMirrorVehicleWorkflowCode(sinks, "pipeline-calculation", entry)
}

/** Where mirrorVehicleWorkflowCode hands each record. */
interface MirrorVehicleWorkflowCodeTarget {
    suspend fun transmit(rec: MirrorVehicleWorkflowCodeRecord)
}

/** What mirrorVehicleWorkflowCode keeps of each RetryVehicleWorkflow response. */
data class MirrorVehicleWorkflowCodeRecord(val code: String, val correlationId: String, val status: String)

/** Passes records to acme.maintenance.v1.WorkOrderCalculationService. */
class MirrorVehicleWorkflowCodeWorkOrderCalculation(private val stub: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub) : MirrorVehicleWorkflowCodeTarget {
    override suspend fun transmit(rec: MirrorVehicleWorkflowCodeRecord) {
        val request = explainWorkOrderCalculationRequest {
            this.workOrder = workOrder { this.subject = rec.code; this.workOrderId = rec.status }
            this.workOrderId = rec.correlationId
        }
        stub.explainWorkOrderCalculation(request)
    }
}

/** Sends records to acme.pipelines.v1.PipelineCalculationService. */
class MirrorVehicleWorkflowCodePipelineCalculation(private val stub: PipelineCalculationServiceGrpcKt.PipelineCalculationServiceCoroutineStub) : MirrorVehicleWorkflowCodeTarget {
    override suspend fun transmit(rec: MirrorVehicleWorkflowCodeRecord) {
        val request = calculatePipelineRequest {
            this.reason = rec.correlationId
            this.pipeline = pipeline { this.pipelineId = rec.status }
        }
        stub.calculatePipeline(request)
    }
}

/** Sends records to acme.dispatch.v1.DispatchOrderWorkflowService. */
class MirrorVehicleWorkflowCodeDispatchOrderWorkflow(private val stub: DispatchOrderWorkflowServiceGrpcKt.DispatchOrderWorkflowServiceCoroutineStub) : MirrorVehicleWorkflowCodeTarget {
    override suspend fun transmit(rec: MirrorVehicleWorkflowCodeRecord) {
        val request = listDispatchOrderWorkflowsRequest {
            this.filter = ListDispatchOrderWorkflowsRequestKt.filter { this.ownerId = rec.code }
        }
        stub.listDispatchOrderWorkflows(request)
    }
}

/** Hands records to acme.orders.v1.OrderService. */
class MirrorVehicleWorkflowCodeOrder(private val stub: OrderServiceGrpcKt.OrderServiceCoroutineStub) : MirrorVehicleWorkflowCodeTarget {
    override suspend fun transmit(rec: MirrorVehicleWorkflowCodeRecord) {
        stub.getOrder(getOrderRequest { this.orderId = rec.correlationId })
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeMirrorVehicleWorkflowCode(sinks: Map<String, MirrorVehicleWorkflowCodeTarget>, kind: String, rec: MirrorVehicleWorkflowCodeRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.transmit(rec)
}
