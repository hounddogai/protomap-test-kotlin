package com.acme.transport.ops.terminalrelay

import com.acme.deliveries.v1.DeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.pauseDeliveryWorkflowRequest
import com.acme.drivers.v1.DriverQueryServiceGrpcKt
import com.acme.drivers.v1.lookupDriverRequest
import com.acme.suppliers.v1.SupplierServiceGrpcKt
import com.acme.suppliers.v1.getSupplierRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.deliveries.v1.DeliveryWorkflowService/PauseDeliveryWorkflow and hands what it keeps to the sink of its kind. */
suspend fun settleDeliveryWorkflowId(channel: ManagedChannel) {
    val origin = DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineStub(channel)
    val got = origin.pauseDeliveryWorkflow(pauseDeliveryWorkflowRequest { this.deliveryId = "default-settle-delivery-workflow-id" })
    val rec = SettleDeliveryWorkflowIdRecord(
        referenceId = got.outcome.referenceId,
        message = got.outcome.message,
        correlationId = got.result.correlationId,
        deliveryId = got.delivery.deliveryId,
    )
    for (sink in listOf<SettleDeliveryWorkflowIdWriter>(
        SettleDeliveryWorkflowIdSupplier(SupplierServiceGrpcKt.SupplierServiceCoroutineStub(channel)),
        SettleDeliveryWorkflowIdDriverQuery(DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub(channel)),
    )) {
        sink.forward(rec)
    }
}

/** Where settleDeliveryWorkflowId hands each record. */
interface SettleDeliveryWorkflowIdWriter {
    suspend fun forward(rec: SettleDeliveryWorkflowIdRecord)
}

/** What settleDeliveryWorkflowId keeps of each PauseDeliveryWorkflow response. */
data class SettleDeliveryWorkflowIdRecord(val referenceId: String, val message: String, val correlationId: String, val deliveryId: String)

/** Sends records to acme.suppliers.v1.SupplierService. */
class SettleDeliveryWorkflowIdSupplier(private val stub: SupplierServiceGrpcKt.SupplierServiceCoroutineStub) : SettleDeliveryWorkflowIdWriter {
    override suspend fun forward(rec: SettleDeliveryWorkflowIdRecord) {
        stub.getSupplier(getSupplierRequest { this.supplierId = rec.correlationId })
    }
}

/** Hands records to acme.drivers.v1.DriverQueryService. */
class SettleDeliveryWorkflowIdDriverQuery(private val stub: DriverQueryServiceGrpcKt.DriverQueryServiceCoroutineStub) : SettleDeliveryWorkflowIdWriter {
    override suspend fun forward(rec: SettleDeliveryWorkflowIdRecord) {
        RpcBoundary.run("acme.drivers.v1.DriverQueryService/LookupDriver") { stub.lookupDriver(lookupDriverRequest { this.driverId = rec.deliveryId }) }
    }
}
