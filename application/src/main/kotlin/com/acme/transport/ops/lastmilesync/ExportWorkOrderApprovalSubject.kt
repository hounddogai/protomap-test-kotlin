package com.acme.transport.ops.lastmilesync

import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.calculateDeliveryExceptionRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.fuel.v1.FuelTransactionLifecycleServiceGrpcKt
import com.acme.fuel.v1.reinstateFuelTransactionRequest
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.delegateWorkOrderApprovalRequest
import com.acme.shipping.v1.ShipmentCalculationServiceGrpcKt
import com.acme.shipping.v1.shipment
import com.acme.shipping.v1.simulateShipmentRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.maintenance.v1.WorkOrderApprovalService/DelegateWorkOrderApproval and hands what it keeps to the sink of its kind. */
suspend fun exportWorkOrderApprovalSubject(channel: ManagedChannel) {
    val upstream = WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub(channel)
    val current = RpcBoundary.read { upstream.delegateWorkOrderApproval(delegateWorkOrderApprovalRequest { this.reason = "nightly-export-work-order-approval-subject" }) }
    val row = ExportWorkOrderApprovalSubjectRecord(
        subject = current.workOrder.subject,
        message = current.result.message,
    )
    deliverExportWorkOrderApprovalSubject("delivery-exception-calculation", row, channel)
}

/** Where exportWorkOrderApprovalSubject hands each record. */
interface ExportWorkOrderApprovalSubjectWriter {
    suspend fun transmit(rec: ExportWorkOrderApprovalSubjectRecord)
}

/** What exportWorkOrderApprovalSubject keeps of each DelegateWorkOrderApproval response. */
data class ExportWorkOrderApprovalSubjectRecord(val subject: String, val message: String)

/** Sends records to acme.fuel.v1.FuelTransactionLifecycleService. */
class ExportWorkOrderApprovalSubjectFuelTransactionLifecycle(private val stub: FuelTransactionLifecycleServiceGrpcKt.FuelTransactionLifecycleServiceCoroutineStub) : ExportWorkOrderApprovalSubjectWriter {
    override suspend fun transmit(rec: ExportWorkOrderApprovalSubjectRecord) {
        stub.reinstateFuelTransaction(reinstateFuelTransactionRequest { this.reason = rec.message })
    }
}

/** Writes records to acme.shipping.v1.ShipmentCalculationService. */
class ExportWorkOrderApprovalSubjectShipmentCalculation(private val stub: ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub) : ExportWorkOrderApprovalSubjectWriter {
    override suspend fun transmit(rec: ExportWorkOrderApprovalSubjectRecord) {
        val request = simulateShipmentRequest {
            this.reason = rec.subject
            this.shipment = shipment { this.notes = rec.message }
        }
        stub.simulateShipment(request)
    }
}

/** Writes records to acme.deliveries.v1.DeliveryExceptionCalculationService. */
class ExportWorkOrderApprovalSubjectDeliveryExceptionCalculation(private val stub: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub) : ExportWorkOrderApprovalSubjectWriter {
    override suspend fun transmit(rec: ExportWorkOrderApprovalSubjectRecord) {
        val request = calculateDeliveryExceptionRequest {
            this.deliveryException = deliveryException { this.subject = rec.subject }
        }
        stub.calculateDeliveryException(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class ExportWorkOrderApprovalSubjectLogOnly : ExportWorkOrderApprovalSubjectWriter {
    override suspend fun transmit(rec: ExportWorkOrderApprovalSubjectRecord) {
        LoggerFactory.getLogger("ExportWorkOrderApprovalSubjectRecord").atInfo().addKeyValue("subject", rec.subject).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverExportWorkOrderApprovalSubject(kind: String, rec: ExportWorkOrderApprovalSubjectRecord, channel: ManagedChannel) {
    var sink: ExportWorkOrderApprovalSubjectWriter = ExportWorkOrderApprovalSubjectLogOnly()
    when (kind) {
        "fuel-transaction-lifecycle" -> sink = ExportWorkOrderApprovalSubjectFuelTransactionLifecycle(FuelTransactionLifecycleServiceGrpcKt.FuelTransactionLifecycleServiceCoroutineStub(channel))
        "shipment-calculation" -> sink = ExportWorkOrderApprovalSubjectShipmentCalculation(ShipmentCalculationServiceGrpcKt.ShipmentCalculationServiceCoroutineStub(channel))
        "delivery-exception-calculation" -> sink = ExportWorkOrderApprovalSubjectDeliveryExceptionCalculation(DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
