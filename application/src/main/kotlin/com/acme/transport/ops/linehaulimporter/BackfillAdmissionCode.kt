package com.acme.transport.ops.linehaulimporter

import com.acme.admissions.v1.AdmissionCalculationServiceGrpcKt
import com.acme.admissions.v1.simulateAdmissionRequest
import com.acme.drivers.v1.DriverLifecycleServiceGrpcKt
import com.acme.drivers.v1.activateDriverRequest
import com.acme.fuel.v1.FuelTransactionWorkflowServiceGrpcKt
import com.acme.fuel.v1.ListFuelTransactionWorkflowsRequestKt
import com.acme.fuel.v1.listFuelTransactionWorkflowsRequest
import com.acme.ledger.v1.LedgerAccountValidationServiceGrpcKt
import com.acme.ledger.v1.previewLedgerAccountRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.admissions.v1.AdmissionCalculationService/SimulateAdmission and hands what it keeps to the sink of its kind. */
suspend fun backfillAdmissionCode(channel: ManagedChannel) {
    val reader = AdmissionCalculationServiceGrpcKt.AdmissionCalculationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { reader.simulateAdmission(simulateAdmissionRequest { this.admissionId = "primary-backfill-admission-code" }) }
    val record = BackfillAdmissionCodeRecord(
        currencyCode = normalize(got.breakdown.currencyCode),
        correlationId = normalize(got.result.correlationId),
    )
    for (sink in listOf<BackfillAdmissionCodeSink>(
        BackfillAdmissionCodeFuelTransactionWorkflow(FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub(channel)),
        BackfillAdmissionCodeDriverLifecycle(DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub(channel)),
        BackfillAdmissionCodeLedgerAccountValidation(LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub(channel)),
    )) {
        sink.deliver(record)
    }
}

/** Where backfillAdmissionCode hands each record. */
interface BackfillAdmissionCodeSink {
    suspend fun deliver(rec: BackfillAdmissionCodeRecord)
}

/** What backfillAdmissionCode keeps of each SimulateAdmission response. */
data class BackfillAdmissionCodeRecord(val currencyCode: String, val correlationId: String)

/** Hands records to acme.fuel.v1.FuelTransactionWorkflowService. */
class BackfillAdmissionCodeFuelTransactionWorkflow(private val stub: FuelTransactionWorkflowServiceGrpcKt.FuelTransactionWorkflowServiceCoroutineStub) : BackfillAdmissionCodeSink {
    override suspend fun deliver(rec: BackfillAdmissionCodeRecord) {
        val request = listFuelTransactionWorkflowsRequest {
            this.filter = ListFuelTransactionWorkflowsRequestKt.filter { this.ownerId = rec.currencyCode }
        }
        RpcBoundary.run("acme.fuel.v1.FuelTransactionWorkflowService/ListFuelTransactionWorkflows") { stub.listFuelTransactionWorkflows(request) }
    }
}

/** Passes records to acme.drivers.v1.DriverLifecycleService. */
class BackfillAdmissionCodeDriverLifecycle(private val stub: DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub) : BackfillAdmissionCodeSink {
    override suspend fun deliver(rec: BackfillAdmissionCodeRecord) {
        RpcBoundary.run("acme.drivers.v1.DriverLifecycleService/ActivateDriver") { stub.activateDriver(activateDriverRequest { this.driverId = rec.correlationId }) }
    }
}

/** Writes records to acme.ledger.v1.LedgerAccountValidationService. */
class BackfillAdmissionCodeLedgerAccountValidation(private val stub: LedgerAccountValidationServiceGrpcKt.LedgerAccountValidationServiceCoroutineStub) : BackfillAdmissionCodeSink {
    override suspend fun deliver(rec: BackfillAdmissionCodeRecord) {
        stub.previewLedgerAccount(previewLedgerAccountRequest { this.ledgerAccountId = rec.currencyCode })
    }
}
