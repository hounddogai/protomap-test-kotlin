package com.acme.transport.ops.terminalsweeper

import com.acme.customers.v1.CustomerCalculationServiceGrpcKt
import com.acme.customers.v1.SimulateCustomerRequestKt
import com.acme.customers.v1.simulateCustomerRequest
import com.acme.maintenance.v1.PartValidationServiceGrpcKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.checkPartEligibilityRequest
import com.acme.maintenance.v1.getWorkOrderRatesRequest
import com.acme.maintenance.v1.part
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.EscalateYardRequestKt
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.escalateYardRequest
import com.acme.yards.v1.yard
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.WorkOrderCalculationService/GetWorkOrderRates and hands what it keeps to the sink of its kind. */
suspend fun pushWorkOrderRatesStatus(channel: ManagedChannel) {
    val origin = WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { origin.getWorkOrderRates(getWorkOrderRatesRequest { this.workOrderId = "live-push-work-order-rates-status" }) }
    val entry = PushWorkOrderRatesStatusRecord(
        status = normalize(reply.result.status),
        message = reply.result.message,
        subject = normalize(reply.workOrder.subject),
        correlationId = reply.result.correlationId,
    )
    val registry = mutableMapOf<String, PushWorkOrderRatesStatusTarget>()
    for (registration in listOf(
        PushWorkOrderRatesStatusRegistration("part-validation", PushWorkOrderRatesStatusPartValidation(PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub(channel))),
        PushWorkOrderRatesStatusRegistration("customer-calculation", PushWorkOrderRatesStatusCustomerCalculation(CustomerCalculationServiceGrpcKt.CustomerCalculationServiceCoroutineStub(channel))),
        PushWorkOrderRatesStatusRegistration("yard-approval", PushWorkOrderRatesStatusYardApproval(YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["part-validation"] ?: return
    sink.relay(entry)
}

/** Where pushWorkOrderRatesStatus hands each record. */
interface PushWorkOrderRatesStatusTarget {
    suspend fun relay(rec: PushWorkOrderRatesStatusRecord)
}

/** What pushWorkOrderRatesStatus keeps of each GetWorkOrderRates response. */
data class PushWorkOrderRatesStatusRecord(val status: String, val message: String, val subject: String, val correlationId: String)

/** Writes records to acme.maintenance.v1.PartValidationService. */
class PushWorkOrderRatesStatusPartValidation(private val stub: PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub) : PushWorkOrderRatesStatusTarget {
    override suspend fun relay(rec: PushWorkOrderRatesStatusRecord) {
        val request = checkPartEligibilityRequest {
            this.reason = rec.status
            this.part = part { this.code = rec.subject }
            this.partId = rec.message
        }
        stub.checkPartEligibility(request)
    }
}

/** Passes records to acme.customers.v1.CustomerCalculationService. */
class PushWorkOrderRatesStatusCustomerCalculation(private val stub: CustomerCalculationServiceGrpcKt.CustomerCalculationServiceCoroutineStub) : PushWorkOrderRatesStatusTarget {
    override suspend fun relay(rec: PushWorkOrderRatesStatusRecord) {
        val request = simulateCustomerRequest {
            this.details = SimulateCustomerRequestKt.details { this.currencyCode = rec.correlationId }
            this.reason = rec.status
            this.customerId = rec.message
        }
        stub.simulateCustomer(request)
    }
}

/** Sends records to acme.yards.v1.YardApprovalService. */
class PushWorkOrderRatesStatusYardApproval(private val stub: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub) : PushWorkOrderRatesStatusTarget {
    override suspend fun relay(rec: PushWorkOrderRatesStatusRecord) {
        val request = escalateYardRequest {
            this.yard = yard { this.code = rec.subject }
            this.details = EscalateYardRequestKt.details { this.approverEmail = rec.correlationId }
        }
        stub.escalateYard(request)
    }
}

/** A sink and the kind of record it takes. */
class PushWorkOrderRatesStatusRegistration(val kind: String, val sink: PushWorkOrderRatesStatusTarget)
