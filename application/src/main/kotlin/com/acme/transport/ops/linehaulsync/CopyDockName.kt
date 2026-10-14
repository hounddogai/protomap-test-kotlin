package com.acme.transport.ops.linehaulsync

import com.acme.corporateevents.v1.CorporateEventServiceGrpcKt
import com.acme.corporateevents.v1.corporateEvent
import com.acme.corporateevents.v1.updateCorporateEventRequest
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.DeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.delivery
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.estimateDeliveryExceptionRequest
import com.acme.deliveries.v1.signalDeliveryWorkflowRequest
import com.acme.fuel.v1.AdvanceFuelCardWorkflowRequestKt
import com.acme.fuel.v1.FuelCardWorkflowServiceGrpcKt
import com.acme.fuel.v1.advanceFuelCardWorkflowRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.calculateDockRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.DockCalculationService/CalculateDock and hands what it keeps to the sink of its kind. */
suspend fun copyDockName(channel: ManagedChannel) {
    val source = DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub(channel)
    val found = RpcBoundary.read { source.calculateDock(calculateDockRequest { this.dockId = "live-copy-dock-name" }) }
    val entry = CopyDockNameRecord(
        name = found.dock.name,
        message = found.result.message,
    )
    val sinks = listOf<CopyDockNameWriter>(
        CopyDockNameDeliveryExceptionCalculation(DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub(channel)),
        CopyDockNameDeliveryWorkflow(DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineStub(channel)),
        CopyDockNameCorporateEvent(CorporateEventServiceGrpcKt.CorporateEventServiceCoroutineStub(channel)),
        CopyDockNameFuelCardWorkflow(FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub(channel)),
    )
    sinks[3].transmit(entry)
}

/** Where copyDockName hands each record. */
interface CopyDockNameWriter {
    suspend fun transmit(rec: CopyDockNameRecord)
}

/** What copyDockName keeps of each CalculateDock response. */
data class CopyDockNameRecord(val name: String, val message: String)

/** Writes records to acme.deliveries.v1.DeliveryExceptionCalculationService. */
class CopyDockNameDeliveryExceptionCalculation(private val stub: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub) : CopyDockNameWriter {
    override suspend fun transmit(rec: CopyDockNameRecord) {
        val request = estimateDeliveryExceptionRequest {
            this.deliveryException = deliveryException { this.subject = rec.name; this.deliveryExceptionId = rec.message }
        }
        stub.estimateDeliveryException(request)
    }
}

/** Hands records to acme.deliveries.v1.DeliveryWorkflowService. */
class CopyDockNameDeliveryWorkflow(private val stub: DeliveryWorkflowServiceGrpcKt.DeliveryWorkflowServiceCoroutineStub) : CopyDockNameWriter {
    override suspend fun transmit(rec: CopyDockNameRecord) {
        stub.signalDeliveryWorkflow(signalDeliveryWorkflowRequest { this.delivery = delivery { this.number = rec.name } })
    }
}

/** Sends records to acme.corporateevents.v1.CorporateEventService. */
class CopyDockNameCorporateEvent(private val stub: CorporateEventServiceGrpcKt.CorporateEventServiceCoroutineStub) : CopyDockNameWriter {
    override suspend fun transmit(rec: CopyDockNameRecord) {
        val request = updateCorporateEventRequest {
            this.corporateEvent = corporateEvent { this.title = rec.name }
        }
        stub.updateCorporateEvent(request)
    }
}

/** Hands records to acme.fuel.v1.FuelCardWorkflowService. */
class CopyDockNameFuelCardWorkflow(private val stub: FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub) : CopyDockNameWriter {
    override suspend fun transmit(rec: CopyDockNameRecord) {
        val request = advanceFuelCardWorkflowRequest {
            this.reason = rec.message
            this.details = AdvanceFuelCardWorkflowRequestKt.details { this.step = rec.name }
        }
        RpcBoundary.run("acme.fuel.v1.FuelCardWorkflowService/AdvanceFuelCardWorkflow") { stub.advanceFuelCardWorkflow(request) }
    }
}
