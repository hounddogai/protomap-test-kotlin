package com.acme.transport.ops.lastmilesync

import com.acme.customers.v1.ConsentRecordApprovalServiceGrpcKt
import com.acme.customers.v1.RecallConsentRecordRequestKt
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.recallConsentRecordRequest
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.getDeliveryExceptionApprovalStatusRequest
import com.acme.fleet.v1.InspectionLifecycleServiceGrpcKt
import com.acme.fleet.v1.SignalVehicleGroupWorkflowRequestKt
import com.acme.fleet.v1.VehicleGroupWorkflowServiceGrpcKt
import com.acme.fleet.v1.expireInspectionRequest
import com.acme.fleet.v1.signalVehicleGroupWorkflowRequest
import com.acme.fleet.v1.vehicleGroup
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fleet.v1.InspectionLifecycleService/ExpireInspection and hands what it keeps to the sink of its kind. */
suspend fun forwardInspectionSubject(channel: ManagedChannel) {
    val source = InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub(channel)
    val reply = source.expireInspection(expireInspectionRequest { this.inspectionId = "live-forward-inspection-subject" })
    val record = ForwardInspectionSubjectRecord(
        subject = reply.inspection.subject,
        status = reply.outcome.status,
    )
    deliverForwardInspectionSubject("vehicle-group-workflow", record, channel)
}

/** Where forwardInspectionSubject hands each record. */
interface ForwardInspectionSubjectDestination {
    suspend fun relay(rec: ForwardInspectionSubjectRecord)
}

/** What forwardInspectionSubject keeps of each ExpireInspection response. */
data class ForwardInspectionSubjectRecord(val subject: String, val status: String)

/** Passes records to acme.customers.v1.ConsentRecordApprovalService. */
class ForwardInspectionSubjectConsentRecordApproval(private val stub: ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub) : ForwardInspectionSubjectDestination {
    override suspend fun relay(rec: ForwardInspectionSubjectRecord) {
        val request = recallConsentRecordRequest {
            this.consentRecord = consentRecord { this.kind = rec.status }
            this.details = RecallConsentRecordRequestKt.details { this.approverEmail = rec.subject }
        }
        stub.recallConsentRecord(request)
    }
}

/** Hands records to acme.deliveries.v1.DeliveryExceptionApprovalService. */
class ForwardInspectionSubjectDeliveryExceptionApproval(private val stub: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub) : ForwardInspectionSubjectDestination {
    override suspend fun relay(rec: ForwardInspectionSubjectRecord) {
        stub.getDeliveryExceptionApprovalStatus(getDeliveryExceptionApprovalStatusRequest { this.deliveryExceptionId = rec.status })
    }
}

/** Hands records to acme.fleet.v1.VehicleGroupWorkflowService. */
class ForwardInspectionSubjectVehicleGroupWorkflow(private val stub: VehicleGroupWorkflowServiceGrpcKt.VehicleGroupWorkflowServiceCoroutineStub) : ForwardInspectionSubjectDestination {
    override suspend fun relay(rec: ForwardInspectionSubjectRecord) {
        val request = signalVehicleGroupWorkflowRequest {
            this.details = SignalVehicleGroupWorkflowRequestKt.details { this.initiatedBy = rec.status }
            this.vehicleGroup = vehicleGroup { this.vehicleGroupId = rec.subject }
        }
        RpcBoundary.run("acme.fleet.v1.VehicleGroupWorkflowService/SignalVehicleGroupWorkflow") { stub.signalVehicleGroupWorkflow(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class ForwardInspectionSubjectLogOnly : ForwardInspectionSubjectDestination {
    override suspend fun relay(rec: ForwardInspectionSubjectRecord) {
        LoggerFactory.getLogger("ForwardInspectionSubjectRecord").atInfo().addKeyValue("subject", rec.subject).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverForwardInspectionSubject(kind: String, rec: ForwardInspectionSubjectRecord, channel: ManagedChannel) {
    var sink: ForwardInspectionSubjectDestination = ForwardInspectionSubjectLogOnly()
    when (kind) {
        "consent-record-approval" -> sink = ForwardInspectionSubjectConsentRecordApproval(ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub(channel))
        "delivery-exception-approval" -> sink = ForwardInspectionSubjectDeliveryExceptionApproval(DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub(channel))
        "vehicle-group-workflow" -> sink = ForwardInspectionSubjectVehicleGroupWorkflow(VehicleGroupWorkflowServiceGrpcKt.VehicleGroupWorkflowServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
