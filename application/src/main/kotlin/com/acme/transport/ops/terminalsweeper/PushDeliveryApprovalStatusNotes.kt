package com.acme.transport.ops.terminalsweeper

import com.acme.deliveries.v1.CancelDeliveryExceptionWorkflowRequestKt
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.DeliveryExceptionWorkflowServiceGrpcKt
import com.acme.deliveries.v1.cancelDeliveryExceptionWorkflowRequest
import com.acme.deliveries.v1.getDeliveryApprovalStatusRequest
import com.acme.fleet.v1.StartVehicleWorkflowRequestKt
import com.acme.fleet.v1.VehicleWorkflowServiceGrpcKt
import com.acme.fleet.v1.startVehicleWorkflowRequest
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.comparePartOptionsRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.deliveries.v1.DeliveryApprovalService/GetDeliveryApprovalStatus and hands what it keeps to the sink of its kind. */
suspend fun pushDeliveryApprovalStatusNotes(channel: ManagedChannel) {
    val upstream = DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub(channel)
    val current = upstream.getDeliveryApprovalStatus(getDeliveryApprovalStatusRequest { this.deliveryId = "live-push-delivery-approval-status-notes" })
    val rec = PushDeliveryApprovalStatusNotesRecord(
        notes = current.delivery.notes,
        correlationId = current.result.correlationId,
    )
    deliverPushDeliveryApprovalStatusNotes("vehicle-workflow", rec, channel)
}

/** Where pushDeliveryApprovalStatusNotes hands each record. */
interface PushDeliveryApprovalStatusNotesDestination {
    suspend fun write(rec: PushDeliveryApprovalStatusNotesRecord)
}

/** What pushDeliveryApprovalStatusNotes keeps of each GetDeliveryApprovalStatus response. */
data class PushDeliveryApprovalStatusNotesRecord(val notes: String, val correlationId: String)

/** Passes records to acme.fleet.v1.VehicleWorkflowService. */
class PushDeliveryApprovalStatusNotesVehicleWorkflow(private val stub: VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub) : PushDeliveryApprovalStatusNotesDestination {
    override suspend fun write(rec: PushDeliveryApprovalStatusNotesRecord) {
        val request = startVehicleWorkflowRequest {
            this.details = StartVehicleWorkflowRequestKt.details { this.initiatedBy = rec.correlationId }
        }
        stub.startVehicleWorkflow(request)
    }
}

/** Sends records to acme.deliveries.v1.DeliveryExceptionWorkflowService. */
class PushDeliveryApprovalStatusNotesDeliveryExceptionWorkflow(private val stub: DeliveryExceptionWorkflowServiceGrpcKt.DeliveryExceptionWorkflowServiceCoroutineStub) : PushDeliveryApprovalStatusNotesDestination {
    override suspend fun write(rec: PushDeliveryApprovalStatusNotesRecord) {
        val request = cancelDeliveryExceptionWorkflowRequest {
            this.details = CancelDeliveryExceptionWorkflowRequestKt.details { this.step = rec.notes }
        }
        stub.cancelDeliveryExceptionWorkflow(request)
    }
}

/** Sends records to acme.maintenance.v1.PartCalculationService. */
class PushDeliveryApprovalStatusNotesPartCalculation(private val stub: PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub) : PushDeliveryApprovalStatusNotesDestination {
    override suspend fun write(rec: PushDeliveryApprovalStatusNotesRecord) {
        stub.comparePartOptions(comparePartOptionsRequest { this.reason = rec.notes })
    }
}

/** Only logs, for kinds no service takes yet. */
class PushDeliveryApprovalStatusNotesLogOnly : PushDeliveryApprovalStatusNotesDestination {
    override suspend fun write(rec: PushDeliveryApprovalStatusNotesRecord) {
        LoggerFactory.getLogger("PushDeliveryApprovalStatusNotesRecord").atInfo().addKeyValue("notes", rec.notes).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverPushDeliveryApprovalStatusNotes(kind: String, rec: PushDeliveryApprovalStatusNotesRecord, channel: ManagedChannel) {
    var sink: PushDeliveryApprovalStatusNotesDestination = PushDeliveryApprovalStatusNotesLogOnly()
    when (kind) {
        "vehicle-workflow" -> sink = PushDeliveryApprovalStatusNotesVehicleWorkflow(VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub(channel))
        "delivery-exception-workflow" -> sink = PushDeliveryApprovalStatusNotesDeliveryExceptionWorkflow(DeliveryExceptionWorkflowServiceGrpcKt.DeliveryExceptionWorkflowServiceCoroutineStub(channel))
        "part-calculation" -> sink = PushDeliveryApprovalStatusNotesPartCalculation(PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub(channel))
    }
    sink.write(rec)
}
