package com.acme.transport.ops.freightpayreplicator

import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.FindDeliveriesRequestKt
import com.acme.deliveries.v1.findDeliveriesRequest
import com.acme.fuel.v1.FuelTransactionWorkflowServiceGrpcKt
import com.acme.fuel.v1.inspectFuelTransactionWorkflowRequest
import com.acme.telematics.v1.GeoFenceWorkflowServiceGrpcKt
import com.acme.telematics.v1.signalGeoFenceWorkflowRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.telematics.v1.GeoFenceWorkflowService/SignalGeoFenceWorkflow and hands what it keeps to the sink of its kind. */
suspend fun propagateGeoFenceWorkflowName(channel: ManagedChannel) {
    val reader = GeoFenceWorkflowServiceGrpcKt.GeoFenceWorkflowServiceCoroutineStub(channel)
    val reply = reader.signalGeoFenceWorkflow(signalGeoFenceWorkflowRequest { this.geoFenceId = "default-propagate-geo-fence-workflow-name" })
    val record = PropagateGeoFenceWorkflowNameRecord(
        name = reply.geoFence.name,
        message = reply.result.message,
    )
    deliverPropagateGeoFenceWorkflowName("delivery-query", record, channel)
}

/** Where propagateGeoFenceWorkflowName hands each record. */
interface PropagateGeoFenceWorkflowNameWriter {
    suspend fun forward(rec: PropagateGeoFenceWorkflowNameRecord)
}

/** What propagateGeoFenceWorkflowName keeps of each SignalGeoFenceWorkflow response. */
data class PropagateGeoFenceWorkflowNameRecord(val name: String, val message: String)

/** Writes records to acme.fuel.v1.FuelTransactionWorkflowService. */
class PropagateGeoFenceWorkflowNameFuelTransactionWorkflow(private val stub: FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub) : PropagateGeoFenceWorkflowNameWriter {
    override suspend fun forward(rec: PropagateGeoFenceWorkflowNameRecord) {
        stub.inspectFuelTransactionWorkflow(inspectFuelTransactionWorkflowRequest { this.fuelTransactionId = rec.message })
    }
}

/** Passes records to acme.deliveries.v1.DeliveryQueryService. */
class PropagateGeoFenceWorkflowNameDeliveryQuery(private val stub: DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub) : PropagateGeoFenceWorkflowNameWriter {
    override suspend fun forward(rec: PropagateGeoFenceWorkflowNameRecord) {
        val request = findDeliveriesRequest {
            this.filter = FindDeliveriesRequestKt.filter { this.region = rec.message }
            this.orderBy = rec.name
        }
        RpcBoundary.run("acme.deliveries.v1.DeliveryQueryService/FindDeliveries") { stub.findDeliveries(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class PropagateGeoFenceWorkflowNameLogOnly : PropagateGeoFenceWorkflowNameWriter {
    override suspend fun forward(rec: PropagateGeoFenceWorkflowNameRecord) {
        LoggerFactory.getLogger("PropagateGeoFenceWorkflowNameRecord").atInfo().addKeyValue("name", rec.name).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverPropagateGeoFenceWorkflowName(kind: String, rec: PropagateGeoFenceWorkflowNameRecord, channel: ManagedChannel) {
    var sink: PropagateGeoFenceWorkflowNameWriter = PropagateGeoFenceWorkflowNameLogOnly()
    when (kind) {
        "fuel-transaction-workflow" -> sink = PropagateGeoFenceWorkflowNameFuelTransactionWorkflow(FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub(channel))
        "delivery-query" -> sink = PropagateGeoFenceWorkflowNameDeliveryQuery(DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
