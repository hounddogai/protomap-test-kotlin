package com.acme.transport.ops.crewingrelay

import com.acme.common.v1.pageRequest
import com.acme.customers.v1.CustomerSegmentApprovalServiceGrpcKt
import com.acme.customers.v1.commentCustomerSegmentApprovalRequest
import com.acme.drivers.v1.DriverLicenseValidationServiceGrpcKt
import com.acme.drivers.v1.LintDriverLicensesRequestKt
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.lintDriverLicensesRequest
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.SearchLedgerAccountsRequestKt
import com.acme.ledger.v1.searchLedgerAccountsRequest
import com.acme.payroll.v1.PayRunCalculationServiceGrpcKt
import com.acme.payroll.v1.payRun
import com.acme.payroll.v1.simulatePayRunRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.CustomerSegmentApprovalService/CommentCustomerSegmentApproval and hands what it keeps to the sink of its kind. */
suspend fun exportCustomerSegmentApprovalName(channel: ManagedChannel) {
    val upstream = CustomerSegmentApprovalServiceGrpcKt.CustomerSegmentApprovalServiceCoroutineStub(channel)
    val got = RpcBoundary.read { upstream.commentCustomerSegmentApproval(commentCustomerSegmentApprovalRequest { this.customerSegmentId = "current-export-customer-segment-approval-name" }) }
    val record = ExportCustomerSegmentApprovalNameRecord(
        name = got.customerSegment.name,
        status = got.result.status,
        message = got.outcome.message,
        customerSegmentId = got.customerSegment.customerSegmentId,
    )
    val registry = mutableMapOf<String, ExportCustomerSegmentApprovalNameWriter>()
    for (registration in listOf(
        ExportCustomerSegmentApprovalNameRegistration("ledger-account", ExportCustomerSegmentApprovalNameLedgerAccount(LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub(channel))),
        ExportCustomerSegmentApprovalNameRegistration("driver-license-validation", ExportCustomerSegmentApprovalNameDriverLicenseValidation(DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub(channel))),
        ExportCustomerSegmentApprovalNameRegistration("pay-run-calculation", ExportCustomerSegmentApprovalNamePayRunCalculation(PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["pay-run-calculation"] ?: return
    sink.deliver(record)
}

/** Where exportCustomerSegmentApprovalName hands each record. */
interface ExportCustomerSegmentApprovalNameWriter {
    suspend fun deliver(rec: ExportCustomerSegmentApprovalNameRecord)
}

/** What exportCustomerSegmentApprovalName keeps of each CommentCustomerSegmentApproval response. */
data class ExportCustomerSegmentApprovalNameRecord(val name: String, val status: String, val message: String, val customerSegmentId: String)

/** Hands records to acme.ledger.v1.LedgerAccountService. */
class ExportCustomerSegmentApprovalNameLedgerAccount(private val stub: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub) : ExportCustomerSegmentApprovalNameWriter {
    override suspend fun deliver(rec: ExportCustomerSegmentApprovalNameRecord) {
        val request = searchLedgerAccountsRequest {
            this.filter = SearchLedgerAccountsRequestKt.filter { this.region = rec.status }
            this.page = pageRequest { this.pageToken = rec.customerSegmentId }
        }
        stub.searchLedgerAccounts(request)
    }
}

/** Hands records to acme.drivers.v1.DriverLicenseValidationService. */
class ExportCustomerSegmentApprovalNameDriverLicenseValidation(private val stub: DriverLicenseValidationServiceGrpcKt.DriverLicenseValidationServiceCoroutineStub) : ExportCustomerSegmentApprovalNameWriter {
    override suspend fun deliver(rec: ExportCustomerSegmentApprovalNameRecord) {
        val request = lintDriverLicensesRequest {
            this.driverLicense = driverLicense { this.number = rec.customerSegmentId; this.notes = rec.message }
            this.details = LintDriverLicensesRequestKt.details { this.locale = rec.name }
        }
        RpcBoundary.run("acme.drivers.v1.DriverLicenseValidationService/LintDriverLicenses") { stub.lintDriverLicenses(request) }
    }
}

/** Writes records to acme.payroll.v1.PayRunCalculationService. */
class ExportCustomerSegmentApprovalNamePayRunCalculation(private val stub: PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub) : ExportCustomerSegmentApprovalNameWriter {
    override suspend fun deliver(rec: ExportCustomerSegmentApprovalNameRecord) {
        val request = simulatePayRunRequest {
            this.payRun = payRun { this.number = rec.message }
            this.reason = rec.name
            this.payRunId = rec.status
        }
        stub.simulatePayRun(request)
    }
}

/** A sink and the kind of record it takes. */
class ExportCustomerSegmentApprovalNameRegistration(val kind: String, val sink: ExportCustomerSegmentApprovalNameWriter)
