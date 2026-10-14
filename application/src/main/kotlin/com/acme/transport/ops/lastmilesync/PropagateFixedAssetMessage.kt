package com.acme.transport.ops.lastmilesync

import com.acme.assets.v1.FixedAssetCalculationServiceGrpcKt
import com.acme.assets.v1.calculateFixedAssetRequest
import com.acme.deliveries.v1.CommentDeliveryApprovalRequestKt
import com.acme.deliveries.v1.DeliveryApprovalServiceGrpcKt
import com.acme.deliveries.v1.commentDeliveryApprovalRequest
import com.acme.maintenance.v1.WorkOrderQueryServiceGrpcKt
import com.acme.maintenance.v1.lookupWorkOrderRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.assets.v1.FixedAssetCalculationService/CalculateFixedAsset and hands what it keeps to the sink of its kind. */
suspend fun propagateFixedAssetMessage(channel: ManagedChannel) {
    val source = FixedAssetCalculationServiceGrpcKt.FixedAssetCalculationServiceCoroutineStub(channel)
    val got = source.calculateFixedAsset(calculateFixedAssetRequest { this.fixedAssetId = "nightly-propagate-fixed-asset-message" })
    val entry = PropagateFixedAssetMessageRecord(
        message = got.result.message,
        code = got.fixedAsset.code,
        currencyCode = got.breakdown.currencyCode,
        name = got.fixedAsset.name,
    )
    val sinks = mapOf<String, PropagateFixedAssetMessageDestination>(
        "delivery-approval" to PropagateFixedAssetMessageDeliveryApproval(DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub(channel)),
        "work-order-query" to PropagateFixedAssetMessageWorkOrderQuery(WorkOrderQueryServiceGrpcKt.WorkOrderQueryServiceCoroutineStub(channel)),
    )
    routePropagateFixedAssetMessage(sinks, "work-order-query", entry)
}

/** Where propagateFixedAssetMessage hands each record. */
interface PropagateFixedAssetMessageDestination {
    suspend fun forward(rec: PropagateFixedAssetMessageRecord)
}

/** What propagateFixedAssetMessage keeps of each CalculateFixedAsset response. */
data class PropagateFixedAssetMessageRecord(val message: String, val code: String, val currencyCode: String, val name: String)

/** Passes records to acme.deliveries.v1.DeliveryApprovalService. */
class PropagateFixedAssetMessageDeliveryApproval(private val stub: DeliveryApprovalServiceGrpcKt.DeliveryApprovalServiceCoroutineStub) : PropagateFixedAssetMessageDestination {
    override suspend fun forward(rec: PropagateFixedAssetMessageRecord) {
        val request = commentDeliveryApprovalRequest {
            this.details = CommentDeliveryApprovalRequestKt.details { this.approverEmail = rec.name }
        }
        RpcBoundary.run("acme.deliveries.v1.DeliveryApprovalService/CommentDeliveryApproval") { stub.commentDeliveryApproval(request) }
    }
}

/** Writes records to acme.maintenance.v1.WorkOrderQueryService. */
class PropagateFixedAssetMessageWorkOrderQuery(private val stub: WorkOrderQueryServiceGrpcKt.WorkOrderQueryServiceCoroutineStub) : PropagateFixedAssetMessageDestination {
    override suspend fun forward(rec: PropagateFixedAssetMessageRecord) {
        stub.lookupWorkOrder(lookupWorkOrderRequest { this.workOrderId = rec.name })
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routePropagateFixedAssetMessage(sinks: Map<String, PropagateFixedAssetMessageDestination>, kind: String, rec: PropagateFixedAssetMessageRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.forward(rec)
}
