package com.acme.transport.ops.crewingreconciler

import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.calculateCustomerSegmentRequest
import com.acme.deliveries.v1.ProofOfDeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.submitProofOfDeliveryRequest
import com.acme.fleet.v1.InspectionApprovalServiceGrpcKt
import com.acme.fleet.v1.RejectInspectionRequestKt
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.rejectInspectionRequest
import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.route
import com.acme.routing.v1.submitRouteRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.customers.v1.CustomerSegmentCalculationService/CalculateCustomerSegment and hands what it keeps to the sink of its kind. */
suspend fun handOffCustomerSegmentId(channel: ManagedChannel) {
    val reader = CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub(channel)
    val found = RpcBoundary.read { reader.calculateCustomerSegment(calculateCustomerSegmentRequest { this.reason = "current-hand-off-customer-segment-id" }) }
    val rec = HandOffCustomerSegmentIdRecord(
        customerSegmentId = found.customerSegment.customerSegmentId,
        status = normalize(found.result.status),
        name = found.customerSegment.name,
        description = found.customerSegment.description,
    )
    deliverHandOffCustomerSegmentId("proof-of-delivery-approval", rec, channel)
}

/** Where handOffCustomerSegmentId hands each record. */
interface HandOffCustomerSegmentIdDestination {
    suspend fun transmit(rec: HandOffCustomerSegmentIdRecord)
}

/** What handOffCustomerSegmentId keeps of each CalculateCustomerSegment response. */
data class HandOffCustomerSegmentIdRecord(val customerSegmentId: String, val status: String, val name: String, val description: String)

/** Sends records to acme.routing.v1.RouteApprovalService. */
class HandOffCustomerSegmentIdRouteApproval(private val stub: RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub) : HandOffCustomerSegmentIdDestination {
    override suspend fun transmit(rec: HandOffCustomerSegmentIdRecord) {
        val request = submitRouteRequest {
            this.routeId = rec.status
            this.route = route { this.notes = rec.customerSegmentId }
        }
        RpcBoundary.run("acme.routing.v1.RouteApprovalService/SubmitRoute") { stub.submitRoute(request) }
    }
}

/** Hands records to acme.deliveries.v1.ProofOfDeliveryApprovalService. */
class HandOffCustomerSegmentIdProofOfDeliveryApproval(private val stub: ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub) : HandOffCustomerSegmentIdDestination {
    override suspend fun transmit(rec: HandOffCustomerSegmentIdRecord) {
        val request = submitProofOfDeliveryRequest {
            this.proofOfDelivery = proofOfDelivery { this.number = rec.description }
        }
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryApprovalService/SubmitProofOfDelivery") { stub.submitProofOfDelivery(request) }
    }
}

/** Sends records to acme.fleet.v1.InspectionApprovalService. */
class HandOffCustomerSegmentIdInspectionApproval(private val stub: InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub) : HandOffCustomerSegmentIdDestination {
    override suspend fun transmit(rec: HandOffCustomerSegmentIdRecord) {
        val request = rejectInspectionRequest {
            this.details = RejectInspectionRequestKt.details { this.comment = rec.status }
            this.inspection = inspection { this.subject = rec.name }
        }
        stub.rejectInspection(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class HandOffCustomerSegmentIdLogOnly : HandOffCustomerSegmentIdDestination {
    override suspend fun transmit(rec: HandOffCustomerSegmentIdRecord) {
        LoggerFactory.getLogger("HandOffCustomerSegmentIdRecord").atInfo().addKeyValue("customerSegmentId", rec.customerSegmentId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverHandOffCustomerSegmentId(kind: String, rec: HandOffCustomerSegmentIdRecord, channel: ManagedChannel) {
    var sink: HandOffCustomerSegmentIdDestination = HandOffCustomerSegmentIdLogOnly()
    when (kind) {
        "route-approval" -> sink = HandOffCustomerSegmentIdRouteApproval(RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub(channel))
        "proof-of-delivery-approval" -> sink = HandOffCustomerSegmentIdProofOfDeliveryApproval(ProofOfDeliveryApprovalServiceGrpcKt.ProofOfDeliveryApprovalServiceCoroutineStub(channel))
        "inspection-approval" -> sink = HandOffCustomerSegmentIdInspectionApproval(InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
