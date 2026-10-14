package com.acme.transport.ops.lastmilebackfill

import com.acme.driverapp.v1.DriverInboxWorkflowServiceGrpcKt
import com.acme.driverapp.v1.advanceDriverInboxWorkflowRequest
import com.acme.drivers.v1.DriverApprovalServiceGrpcKt
import com.acme.drivers.v1.ListPendingDriversRequestKt
import com.acme.drivers.v1.listPendingDriversRequest
import com.acme.fleet.v1.VehicleGroupApprovalServiceGrpcKt
import com.acme.fleet.v1.submitVehicleGroupRequest
import com.acme.fleet.v1.vehicleGroup
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.driverapp.v1.DriverInboxWorkflowService/AdvanceDriverInboxWorkflow and hands what it keeps to the sink of its kind. */
suspend fun handOffDriverInboxWorkflowMessage(channel: ManagedChannel) {
    val source = DriverInboxWorkflowServiceGrpcKt.DriverInboxWorkflowServiceCoroutineStub(channel)
    val reply = source.advanceDriverInboxWorkflow(advanceDriverInboxWorkflowRequest { this.reason = "live-hand-off-driver-inbox-workflow-message" })
    val rec = HandOffDriverInboxWorkflowMessageRecord(
        message = reply.outcome.message,
        resultMessage = reply.result.message,
    )
    val registry = mutableMapOf<String, HandOffDriverInboxWorkflowMessageSink>()
    for (registration in listOf(
        HandOffDriverInboxWorkflowMessageRegistration("driver-approval", HandOffDriverInboxWorkflowMessageDriverApproval(DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub(channel))),
        HandOffDriverInboxWorkflowMessageRegistration("vehicle-group-approval", HandOffDriverInboxWorkflowMessageVehicleGroupApproval(VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["vehicle-group-approval"] ?: return
    sink.transmit(rec)
}

/** Where handOffDriverInboxWorkflowMessage hands each record. */
interface HandOffDriverInboxWorkflowMessageSink {
    suspend fun transmit(rec: HandOffDriverInboxWorkflowMessageRecord)
}

/** What handOffDriverInboxWorkflowMessage keeps of each AdvanceDriverInboxWorkflow response. */
data class HandOffDriverInboxWorkflowMessageRecord(val message: String, val resultMessage: String)

/** Hands records to acme.drivers.v1.DriverApprovalService. */
class HandOffDriverInboxWorkflowMessageDriverApproval(private val stub: DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub) : HandOffDriverInboxWorkflowMessageSink {
    override suspend fun transmit(rec: HandOffDriverInboxWorkflowMessageRecord) {
        val request = listPendingDriversRequest {
            this.filter = ListPendingDriversRequestKt.filter { this.region = rec.resultMessage }
            this.orderBy = rec.message
        }
        RpcBoundary.run("acme.drivers.v1.DriverApprovalService/ListPendingDrivers") { stub.listPendingDrivers(request) }
    }
}

/** Writes records to acme.fleet.v1.VehicleGroupApprovalService. */
class HandOffDriverInboxWorkflowMessageVehicleGroupApproval(private val stub: VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub) : HandOffDriverInboxWorkflowMessageSink {
    override suspend fun transmit(rec: HandOffDriverInboxWorkflowMessageRecord) {
        val request = submitVehicleGroupRequest {
            this.vehicleGroup = vehicleGroup { this.parentId = rec.resultMessage }
            this.vehicleGroupId = rec.message
        }
        stub.submitVehicleGroup(request)
    }
}

/** A sink and the kind of record it takes. */
class HandOffDriverInboxWorkflowMessageRegistration(val kind: String, val sink: HandOffDriverInboxWorkflowMessageSink)
