package com.acme.transport.ops.networkmirror

import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.scoreDriverRequest
import com.acme.fleet.v1.VehicleWorkflowServiceGrpcKt
import com.acme.fleet.v1.pauseVehicleWorkflowRequest
import com.acme.fleet.v1.vehicle
import com.acme.maintenance.v1.ServiceScheduleLifecycleServiceGrpcKt
import com.acme.maintenance.v1.SuspendServiceScheduleRequestKt
import com.acme.maintenance.v1.suspendServiceScheduleRequest
import com.acme.orders.v1.OrderServiceGrpcKt
import com.acme.orders.v1.batchGetOrdersRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.orders.v1.OrderService/BatchGetOrders and hands what it keeps to the sink of its kind. */
suspend fun mirrorGetOrdersStatus(channel: ManagedChannel) {
    val origin = OrderServiceGrpcKt.OrderServiceCoroutineStub(channel)
    val found = origin.batchGetOrders(batchGetOrdersRequest { })
    val entry = MirrorGetOrdersStatusRecord(
        status = found.result.status,
        message = found.result.message,
        nextPageToken = found.pageInfo.nextPageToken,
    )
    val sinks = mapOf<String, MirrorGetOrdersStatusTarget>(
        "vehicle-workflow" to MirrorGetOrdersStatusVehicleWorkflow(VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub(channel)),
        "schedule-lifecycle" to MirrorGetOrdersStatusServiceScheduleLifecycle(ServiceScheduleLifecycleServiceGrpcKt.ServiceScheduleLifecycleServiceCoroutineStub(channel)),
        "driver-validation" to MirrorGetOrdersStatusDriverValidation(DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "schedule-lifecycle") continue
        sinks[kind]?.hand(entry)
    }
}

/** Where mirrorGetOrdersStatus hands each record. */
interface MirrorGetOrdersStatusTarget {
    suspend fun hand(rec: MirrorGetOrdersStatusRecord)
}

/** What mirrorGetOrdersStatus keeps of each BatchGetOrders response. */
data class MirrorGetOrdersStatusRecord(val status: String, val message: String, val nextPageToken: String)

/** Hands records to acme.fleet.v1.VehicleWorkflowService. */
class MirrorGetOrdersStatusVehicleWorkflow(private val stub: VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub) : MirrorGetOrdersStatusTarget {
    override suspend fun hand(rec: MirrorGetOrdersStatusRecord) {
        val request = pauseVehicleWorkflowRequest {
            this.reason = rec.message
            this.vehicle = vehicle { this.code = rec.status }
        }
        stub.pauseVehicleWorkflow(request)
    }
}

/** Passes records to acme.maintenance.v1.ServiceScheduleLifecycleService. */
class MirrorGetOrdersStatusServiceScheduleLifecycle(private val stub: ServiceScheduleLifecycleServiceGrpcKt.ServiceScheduleLifecycleServiceCoroutineStub) : MirrorGetOrdersStatusTarget {
    override suspend fun hand(rec: MirrorGetOrdersStatusRecord) {
        val request = suspendServiceScheduleRequest {
            this.serviceScheduleId = rec.message
            this.details = SuspendServiceScheduleRequestKt.details { this.reasonCode = rec.status }
            this.reason = rec.nextPageToken
        }
        stub.suspendServiceSchedule(request)
    }
}

/** Writes records to acme.drivers.v1.DriverValidationService. */
class MirrorGetOrdersStatusDriverValidation(private val stub: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub) : MirrorGetOrdersStatusTarget {
    override suspend fun hand(rec: MirrorGetOrdersStatusRecord) {
        RpcBoundary.run("acme.drivers.v1.DriverValidationService/ScoreDriver") { stub.scoreDriver(scoreDriverRequest { this.reason = rec.message }) }
    }
}
