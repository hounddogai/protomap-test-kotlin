package com.acme.transport.ops.safetyexporter

import com.acme.catalog.v1.ProductCalculationServiceGrpcKt
import com.acme.catalog.v1.SimulateProductRequestKt
import com.acme.catalog.v1.simulateProductRequest
import com.acme.customers.v1.CustomerServiceGrpcKt
import com.acme.customers.v1.batchGetCustomersRequest
import com.acme.fuel.v1.FuelCardApprovalServiceGrpcKt
import com.acme.fuel.v1.RejectFuelCardRequestKt
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.rejectFuelCardRequest
import com.acme.routing.v1.ListStopWorkflowsRequestKt
import com.acme.routing.v1.StopWorkflowServiceGrpcKt
import com.acme.routing.v1.listStopWorkflowsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.customers.v1.CustomerService/BatchGetCustomers and hands what it keeps to the sink of its kind. */
suspend fun handOffGetCustomersId(channel: ManagedChannel) {
    val source = CustomerServiceGrpcKt.CustomerServiceCoroutineStub(channel)
    val current = RpcBoundary.read { source.batchGetCustomers(batchGetCustomersRequest { }) }
    val row = HandOffGetCustomersIdRecord(
        correlationId = current.result.correlationId,
        nextPageToken = normalize(current.pageInfo.nextPageToken),
    )
    deliverHandOffGetCustomersId("fuel-card-approval", row, channel)
}

/** Where handOffGetCustomersId hands each record. */
interface HandOffGetCustomersIdWriter {
    suspend fun deliver(rec: HandOffGetCustomersIdRecord)
}

/** What handOffGetCustomersId keeps of each BatchGetCustomers response. */
data class HandOffGetCustomersIdRecord(val correlationId: String, val nextPageToken: String)

/** Hands records to acme.routing.v1.StopWorkflowService. */
class HandOffGetCustomersIdStopWorkflow(private val stub: StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub) : HandOffGetCustomersIdWriter {
    override suspend fun deliver(rec: HandOffGetCustomersIdRecord) {
        val request = listStopWorkflowsRequest {
            this.filter = ListStopWorkflowsRequestKt.filter { this.ownerId = rec.correlationId }
        }
        RpcBoundary.run("acme.routing.v1.StopWorkflowService/ListStopWorkflows") { stub.listStopWorkflows(request) }
    }
}

/** Hands records to acme.catalog.v1.ProductCalculationService. */
class HandOffGetCustomersIdProductCalculation(private val stub: ProductCalculationServiceGrpcKt.ProductCalculationServiceCoroutineStub) : HandOffGetCustomersIdWriter {
    override suspend fun deliver(rec: HandOffGetCustomersIdRecord) {
        val request = simulateProductRequest {
            this.details = SimulateProductRequestKt.details { this.currencyCode = rec.correlationId }
        }
        RpcBoundary.run("acme.catalog.v1.ProductCalculationService/SimulateProduct") { stub.simulateProduct(request) }
    }
}

/** Passes records to acme.fuel.v1.FuelCardApprovalService. */
class HandOffGetCustomersIdFuelCardApproval(private val stub: FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub) : HandOffGetCustomersIdWriter {
    override suspend fun deliver(rec: HandOffGetCustomersIdRecord) {
        val request = rejectFuelCardRequest {
            this.details = RejectFuelCardRequestKt.details { this.comment = rec.correlationId }
            this.fuelCard = fuelCard { this.currencyCode = rec.nextPageToken }
        }
        stub.rejectFuelCard(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class HandOffGetCustomersIdLogOnly : HandOffGetCustomersIdWriter {
    override suspend fun deliver(rec: HandOffGetCustomersIdRecord) {
        LoggerFactory.getLogger("HandOffGetCustomersIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverHandOffGetCustomersId(kind: String, rec: HandOffGetCustomersIdRecord, channel: ManagedChannel) {
    var sink: HandOffGetCustomersIdWriter = HandOffGetCustomersIdLogOnly()
    when (kind) {
        "stop-workflow" -> sink = HandOffGetCustomersIdStopWorkflow(StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub(channel))
        "product-calculation" -> sink = HandOffGetCustomersIdProductCalculation(ProductCalculationServiceGrpcKt.ProductCalculationServiceCoroutineStub(channel))
        "fuel-card-approval" -> sink = HandOffGetCustomersIdFuelCardApproval(FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub(channel))
    }
    sink.deliver(rec)
}
