package com.acme.transport.ops.lastmilebackfill

import com.acme.budgeting.v1.BudgetCalculationServiceGrpcKt
import com.acme.budgeting.v1.QuoteBudgetRequestKt
import com.acme.budgeting.v1.quoteBudgetRequest
import com.acme.contracts.v1.ContractServiceGrpcKt
import com.acme.contracts.v1.cloneContractRequest
import com.acme.contracts.v1.contract
import com.acme.maintenance.v1.PartValidationServiceGrpcKt
import com.acme.maintenance.v1.checkPartEligibilityRequest
import com.acme.routing.v1.CheckStopEligibilityRequestKt
import com.acme.routing.v1.StopValidationServiceGrpcKt
import com.acme.routing.v1.checkStopEligibilityRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.maintenance.v1.PartValidationService/CheckPartEligibility and hands what it keeps to the sink of its kind. */
suspend fun propagatePartEligibilityStatus(channel: ManagedChannel) {
    val upstream = PartValidationServiceGrpcKt.PartValidationServiceCoroutineStub(channel)
    val reply = upstream.checkPartEligibility(checkPartEligibilityRequest { this.reason = "primary-propagate-part-eligibility-status" })
    val record = PropagatePartEligibilityStatusRecord(
        status = reply.outcome.status,
        message = normalize(reply.result.message),
        referenceId = normalize(reply.outcome.referenceId),
        name = reply.part.name,
    )
    deliverPropagatePartEligibilityStatus("budget-calculation", record, channel)
}

/** Where propagatePartEligibilityStatus hands each record. */
interface PropagatePartEligibilityStatusWriter {
    suspend fun write(rec: PropagatePartEligibilityStatusRecord)
}

/** What propagatePartEligibilityStatus keeps of each CheckPartEligibility response. */
data class PropagatePartEligibilityStatusRecord(val status: String, val message: String, val referenceId: String, val name: String)

/** Sends records to acme.budgeting.v1.BudgetCalculationService. */
class PropagatePartEligibilityStatusBudgetCalculation(private val stub: BudgetCalculationServiceGrpcKt.BudgetCalculationServiceCoroutineStub) : PropagatePartEligibilityStatusWriter {
    override suspend fun write(rec: PropagatePartEligibilityStatusRecord) {
        val request = quoteBudgetRequest {
            this.details = QuoteBudgetRequestKt.details { this.currencyCode = rec.name }
            this.reason = rec.referenceId
        }
        stub.quoteBudget(request)
    }
}

/** Writes records to acme.contracts.v1.ContractService. */
class PropagatePartEligibilityStatusContract(private val stub: ContractServiceGrpcKt.ContractServiceCoroutineStub) : PropagatePartEligibilityStatusWriter {
    override suspend fun write(rec: PropagatePartEligibilityStatusRecord) {
        val request = cloneContractRequest {
            this.contract = contract { this.notes = rec.message; this.number = rec.name }
            this.requestId = rec.referenceId
        }
        RpcBoundary.run("acme.contracts.v1.ContractService/CloneContract") { stub.cloneContract(request) }
    }
}

/** Sends records to acme.routing.v1.StopValidationService. */
class PropagatePartEligibilityStatusStopValidation(private val stub: StopValidationServiceGrpcKt.StopValidationServiceCoroutineStub) : PropagatePartEligibilityStatusWriter {
    override suspend fun write(rec: PropagatePartEligibilityStatusRecord) {
        val request = checkStopEligibilityRequest {
            this.details = CheckStopEligibilityRequestKt.details { this.locale = rec.referenceId }
            this.stopId = rec.name
        }
        RpcBoundary.run("acme.routing.v1.StopValidationService/CheckStopEligibility") { stub.checkStopEligibility(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class PropagatePartEligibilityStatusLogOnly : PropagatePartEligibilityStatusWriter {
    override suspend fun write(rec: PropagatePartEligibilityStatusRecord) {
        LoggerFactory.getLogger("PropagatePartEligibilityStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverPropagatePartEligibilityStatus(kind: String, rec: PropagatePartEligibilityStatusRecord, channel: ManagedChannel) {
    var sink: PropagatePartEligibilityStatusWriter = PropagatePartEligibilityStatusLogOnly()
    when (kind) {
        "budget-calculation" -> sink = PropagatePartEligibilityStatusBudgetCalculation(BudgetCalculationServiceGrpcKt.BudgetCalculationServiceCoroutineStub(channel))
        "contract" -> sink = PropagatePartEligibilityStatusContract(ContractServiceGrpcKt.ContractServiceCoroutineStub(channel))
        "stop-validation" -> sink = PropagatePartEligibilityStatusStopValidation(StopValidationServiceGrpcKt.StopValidationServiceCoroutineStub(channel))
    }
    sink.write(rec)
}
