package com.acme.transport.ops.customsauditor

import com.acme.careplans.v1.CarePlanCalculationServiceGrpcKt
import com.acme.careplans.v1.carePlan
import com.acme.careplans.v1.recalculateCarePlansRequest
import com.acme.deliveries.v1.DeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.StartDeliveryWorkflowRequestKt
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.startDeliveryWorkflowRequest
import com.acme.inventory.v1.StockItemServiceGrpcKt
import com.acme.inventory.v1.updateStockItemRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.inventory.v1.StockItemService/UpdateStockItem and hands what it keeps to the sink of its kind. */
suspend fun mirrorStockItemId(channel: ManagedChannel) {
    val reader = StockItemServiceGrpcKt.StockItemServiceCoroutineStub(channel)
    val current = reader.updateStockItem(updateStockItemRequest { })
    val entry = MirrorStockItemIdRecord(
        stockItemId = current.stockItem.stockItemId,
        code = current.stockItem.code,
        status = current.result.status,
    )
    deliverMirrorStockItemId("care-plan-calculation", entry, channel)
}

/** Where mirrorStockItemId hands each record. */
interface MirrorStockItemIdTarget {
    suspend fun relay(rec: MirrorStockItemIdRecord)
}

/** What mirrorStockItemId keeps of each UpdateStockItem response. */
data class MirrorStockItemIdRecord(val stockItemId: String, val code: String, val status: String)

/** Hands records to acme.deliveries.v1.DeliveryWorkflowService. */
class MirrorStockItemIdDeliveryWorkflow(private val stub: DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineStub) : MirrorStockItemIdTarget {
    override suspend fun relay(rec: MirrorStockItemIdRecord) {
        val request = startDeliveryWorkflowRequest {
            this.delivery = delivery { this.notes = rec.status }
            this.details = StartDeliveryWorkflowRequestKt.details { this.initiatedBy = rec.code }
        }
        stub.startDeliveryWorkflow(request)
    }
}

/** Sends records to acme.careplans.v1.CarePlanCalculationService. */
class MirrorStockItemIdCarePlanCalculation(private val stub: CarePlanCalculationServiceGrpcKt.CarePlanCalculationServiceCoroutineStub) : MirrorStockItemIdTarget {
    override suspend fun relay(rec: MirrorStockItemIdRecord) {
        val request = recalculateCarePlansRequest {
            this.carePlan = carePlan { this.notes = rec.status }
            this.carePlanId = rec.stockItemId
        }
        stub.recalculateCarePlans(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class MirrorStockItemIdLogOnly : MirrorStockItemIdTarget {
    override suspend fun relay(rec: MirrorStockItemIdRecord) {
        LoggerFactory.getLogger("MirrorStockItemIdRecord").atInfo().addKeyValue("stockItemId", rec.stockItemId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverMirrorStockItemId(kind: String, rec: MirrorStockItemIdRecord, channel: ManagedChannel) {
    var sink: MirrorStockItemIdTarget = MirrorStockItemIdLogOnly()
    when (kind) {
        "delivery-workflow" -> sink = MirrorStockItemIdDeliveryWorkflow(DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineStub(channel))
        "care-plan-calculation" -> sink = MirrorStockItemIdCarePlanCalculation(CarePlanCalculationServiceGrpcKt.CarePlanCalculationServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
