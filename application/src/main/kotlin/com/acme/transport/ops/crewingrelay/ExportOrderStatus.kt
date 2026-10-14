package com.acme.transport.ops.crewingrelay

import com.acme.contracts.v1.ContractServiceGrpcKt
import com.acme.contracts.v1.getContractRequest
import com.acme.orders.v1.OrderServiceGrpcKt
import com.acme.orders.v1.createOrderRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.treasury.v1.CashPositionCalculationServiceGrpcKt
import com.acme.treasury.v1.cashPosition
import com.acme.treasury.v1.estimateCashPositionRequest
import io.grpc.ManagedChannel

/** Reads acme.orders.v1.OrderService/CreateOrder and hands what it keeps to the sink of its kind. */
suspend fun exportOrderStatus(channel: ManagedChannel) {
    val source = OrderServiceGrpcKt.OrderServiceCoroutineStub(channel)
    val found = RpcBoundary.read { source.createOrder(createOrderRequest { this.requestId = "nightly-export-order-status" }) }
    val rec = ExportOrderStatusRecord(
        status = found.result.status,
        message = normalize(found.result.message),
        notes = found.order.notes,
    )
    val sinks = listOf<ExportOrderStatusWriter>(
        ExportOrderStatusContract(ContractServiceGrpcKt.ContractServiceCoroutineStub(channel)),
        ExportOrderStatusCashPositionCalculation(CashPositionCalculationServiceGrpcKt.CashPositionCalculationServiceCoroutineStub(channel)),
    )
    sinks[1].hand(rec)
}

/** Where exportOrderStatus hands each record. */
interface ExportOrderStatusWriter {
    suspend fun hand(rec: ExportOrderStatusRecord)
}

/** What exportOrderStatus keeps of each CreateOrder response. */
data class ExportOrderStatusRecord(val status: String, val message: String, val notes: String)

/** Hands records to acme.contracts.v1.ContractService. */
class ExportOrderStatusContract(private val stub: ContractServiceGrpcKt.ContractServiceCoroutineStub) : ExportOrderStatusWriter {
    override suspend fun hand(rec: ExportOrderStatusRecord) {
        stub.getContract(getContractRequest { this.contractId = rec.status })
    }
}

/** Hands records to acme.treasury.v1.CashPositionCalculationService. */
class ExportOrderStatusCashPositionCalculation(private val stub: CashPositionCalculationServiceGrpcKt.CashPositionCalculationServiceCoroutineStub) : ExportOrderStatusWriter {
    override suspend fun hand(rec: ExportOrderStatusRecord) {
        val request = estimateCashPositionRequest {
            this.reason = rec.status
            this.cashPosition = cashPosition { this.currencyCode = rec.notes }
        }
        stub.estimateCashPosition(request)
    }
}
