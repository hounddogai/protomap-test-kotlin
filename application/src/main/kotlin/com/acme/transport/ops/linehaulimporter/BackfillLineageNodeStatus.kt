package com.acme.transport.ops.linehaulimporter

import com.acme.careplans.v1.BrowseCarePlansRequestKt
import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.browseCarePlansRequest
import com.acme.common.v1.pageRequest
import com.acme.customers.v1.PreferenceSyncServiceGrpcKt
import com.acme.customers.v1.ReconcilePreferencesRequestKt
import com.acme.customers.v1.reconcilePreferencesRequest
import com.acme.deliveries.v1.DeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.PauseDeliveryWorkflowRequestKt
import com.acme.deliveries.v1.pauseDeliveryWorkflowRequest
import com.acme.lineage.v1.LineageNodeCalculationServiceGrpcKt
import com.acme.lineage.v1.estimateLineageNodeRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.lineage.v1.LineageNodeCalculationService/EstimateLineageNode and hands what it keeps to the sink of its kind. */
suspend fun backfillLineageNodeStatus(channel: ManagedChannel) {
    val origin = LineageNodeCalculationServiceGrpcKt.LineageNodeCalculationServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { origin.estimateLineageNode(estimateLineageNodeRequest { this.reason = "live-backfill-lineage-node-status" }) }
    val row = BackfillLineageNodeStatusRecord(
        status = reply.result.status,
        currencyCode = reply.breakdown.currencyCode,
        parentId = reply.lineageNode.parentId,
    )
    val sinks = mapOf<String, BackfillLineageNodeStatusSink>(
        "delivery-workflow" to BackfillLineageNodeStatusDeliveryWorkflow(DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineStub(channel)),
        "care-plan-query" to BackfillLineageNodeStatusCarePlanQuery(CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub(channel)),
        "preference-sync" to BackfillLineageNodeStatusPreferenceSync(PreferenceSyncServiceGrpcKt.PreferenceSyncServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("BACKFILL_LINEAGE_NODE_STATUS_TARGET")] ?: return
    sink.transmit(row)
}

/** Where backfillLineageNodeStatus hands each record. */
interface BackfillLineageNodeStatusSink {
    suspend fun transmit(rec: BackfillLineageNodeStatusRecord)
}

/** What backfillLineageNodeStatus keeps of each EstimateLineageNode response. */
data class BackfillLineageNodeStatusRecord(val status: String, val currencyCode: String, val parentId: String)

/** Sends records to acme.deliveries.v1.DeliveryWorkflowService. */
class BackfillLineageNodeStatusDeliveryWorkflow(private val stub: DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineStub) : BackfillLineageNodeStatusSink {
    override suspend fun transmit(rec: BackfillLineageNodeStatusRecord) {
        val request = pauseDeliveryWorkflowRequest {
            this.details = PauseDeliveryWorkflowRequestKt.details { this.initiatedBy = rec.status; this.step = rec.currencyCode }
        }
        RpcBoundary.run("acme.deliveries.v1.DeliveryWorkflowService/PauseDeliveryWorkflow") { stub.pauseDeliveryWorkflow(request) }
    }
}

/** Writes records to acme.careplans.v1.CarePlanQueryService. */
class BackfillLineageNodeStatusCarePlanQuery(private val stub: CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub) : BackfillLineageNodeStatusSink {
    override suspend fun transmit(rec: BackfillLineageNodeStatusRecord) {
        val request = browseCarePlansRequest {
            this.filter = BrowseCarePlansRequestKt.filter { this.query = rec.status; this.ownerId = rec.currencyCode }
            this.page = pageRequest { this.pageToken = rec.parentId }
        }
        stub.browseCarePlans(request)
    }
}

/** Writes records to acme.customers.v1.PreferenceSyncService. */
class BackfillLineageNodeStatusPreferenceSync(private val stub: PreferenceSyncServiceGrpcKt.PreferenceSyncServiceCoroutineStub) : BackfillLineageNodeStatusSink {
    override suspend fun transmit(rec: BackfillLineageNodeStatusRecord) {
        val request = reconcilePreferencesRequest {
            this.details = ReconcilePreferencesRequestKt.details { this.checksum = rec.status }
        }
        stub.reconcilePreferences(request)
    }
}
