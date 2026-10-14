package com.acme.transport.ops.crewingreconciler

import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.quoteCustomerSegmentRequest
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.approveDeliveryRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.previewUserRequest
import com.acme.identity.v1.user
import com.acme.telematics.v1.GeoFenceWorkflowServiceGrpcKt
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.retryGeoFenceWorkflowRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.deliveries.v1.DeliveryApprovalService/ApproveDelivery and hands what it keeps to the sink of its kind. */
suspend fun propagateDeliveryStatus(channel: ManagedChannel) {
    val upstream = DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub(channel)
    val current = upstream.approveDelivery(approveDeliveryRequest { this.deliveryId = "current-propagate-delivery-status" })
    val row = PropagateDeliveryStatusRecord(
        status = current.result.status,
        message = current.outcome.message,
    )
    dispatchPropagateDeliveryStatus("user-validation", row, channel)
}

/** Where propagateDeliveryStatus hands each record. */
interface PropagateDeliveryStatusTarget {
    suspend fun deliver(rec: PropagateDeliveryStatusRecord)
}

/** What propagateDeliveryStatus keeps of each ApproveDelivery response. */
data class PropagateDeliveryStatusRecord(val status: String, val message: String)

/** Hands records to acme.telematics.v1.GeoFenceWorkflowService. */
class PropagateDeliveryStatusGeoFenceWorkflow(private val stub: GeoFenceWorkflowServiceGrpcKt.GeoFenceWorkflowServiceCoroutineStub) : PropagateDeliveryStatusTarget {
    override suspend fun deliver(rec: PropagateDeliveryStatusRecord) {
        val request = retryGeoFenceWorkflowRequest {
            this.geoFence = geoFence { this.description = rec.status; this.name = rec.message }
        }
        RpcBoundary.run("acme.telematics.v1.GeoFenceWorkflowService/RetryGeoFenceWorkflow") { stub.retryGeoFenceWorkflow(request) }
    }
}

/** Sends records to acme.customers.v1.CustomerSegmentCalculationService. */
class PropagateDeliveryStatusCustomerSegmentCalculation(private val stub: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub) : PropagateDeliveryStatusTarget {
    override suspend fun deliver(rec: PropagateDeliveryStatusRecord) {
        val request = quoteCustomerSegmentRequest {
            this.customerSegment = customerSegment { this.name = rec.message }
        }
        stub.quoteCustomerSegment(request)
    }
}

/** Passes records to acme.identity.v1.UserValidationService. */
class PropagateDeliveryStatusUserValidation(private val stub: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub) : PropagateDeliveryStatusTarget {
    override suspend fun deliver(rec: PropagateDeliveryStatusRecord) {
        val request = previewUserRequest {
            this.userId = rec.status
            this.user = user { this.userId = rec.message }
        }
        stub.previewUser(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class PropagateDeliveryStatusLogOnly : PropagateDeliveryStatusTarget {
    override suspend fun deliver(rec: PropagateDeliveryStatusRecord) {
        LoggerFactory.getLogger("PropagateDeliveryStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchPropagateDeliveryStatus(kind: String, rec: PropagateDeliveryStatusRecord, channel: ManagedChannel) {
    var write: suspend (PropagateDeliveryStatusRecord) -> Unit = PropagateDeliveryStatusLogOnly()::deliver
    when (kind) {
        "geo-fence-workflow" -> write = PropagateDeliveryStatusGeoFenceWorkflow(GeoFenceWorkflowServiceGrpcKt.GeoFenceWorkflowServiceCoroutineStub(channel))::deliver
        "customer-segment-calculation" -> write = PropagateDeliveryStatusCustomerSegmentCalculation(CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub(channel))::deliver
        "user-validation" -> write = PropagateDeliveryStatusUserValidation(UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub(channel))::deliver
    }
    write(rec)
}
