package com.acme.transport.ops.freightpaysweeper

import com.acme.driverapp.v1.DriverHomeReportingServiceGrpcKt
import com.acme.driverapp.v1.deleteDriverHomeReportRequest
import com.acme.expenses.v1.ExpenseReportCalculationServiceGrpcKt
import com.acme.expenses.v1.quoteExpenseReportRequest
import com.acme.fleet.v1.EscalateInspectionRequestKt
import com.acme.fleet.v1.InspectionApprovalServiceGrpcKt
import com.acme.fleet.v1.escalateInspectionRequest
import com.acme.fleet.v1.inspection
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.RevertFuelPolicyRequestKt
import com.acme.fuel.v1.revertFuelPolicyRequest
import com.acme.performance.v1.FindReviewsRequestKt
import com.acme.performance.v1.ReviewQueryServiceGrpcKt
import com.acme.performance.v1.findReviewsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.expenses.v1.ExpenseReportCalculationService/QuoteExpenseReport and hands what it keeps to the sink of its kind. */
suspend fun exportExpenseReportId(channel: ManagedChannel) {
    val source = ExpenseReportCalculationServiceGrpcKt.ExpenseReportCalculationServiceCoroutineStub(channel)
    val reply = source.quoteExpenseReport(quoteExpenseReportRequest { this.reason = "default-export-expense-report-id" })
    val entry = ExportExpenseReportIdRecord(
        correlationId = reply.result.correlationId,
        currencyCode = normalize(reply.breakdown.currencyCode),
        notes = reply.expenseReport.notes,
        status = reply.result.status,
    )
    dispatchExportExpenseReportId("inspection-approval", entry, channel)
}

/** Where exportExpenseReportId hands each record. */
interface ExportExpenseReportIdSink {
    suspend fun hand(rec: ExportExpenseReportIdRecord)
}

/** What exportExpenseReportId keeps of each QuoteExpenseReport response. */
data class ExportExpenseReportIdRecord(val correlationId: String, val currencyCode: String, val notes: String, val status: String)

/** Writes records to acme.fleet.v1.InspectionApprovalService. */
class ExportExpenseReportIdInspectionApproval(private val stub: InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub) : ExportExpenseReportIdSink {
    override suspend fun hand(rec: ExportExpenseReportIdRecord) {
        val request = escalateInspectionRequest {
            this.inspection = inspection { this.subject = rec.currencyCode }
            this.reason = rec.status
            this.details = EscalateInspectionRequestKt.details { this.approverEmail = rec.notes }
        }
        stub.escalateInspection(request)
    }
}

/** Writes records to acme.fuel.v1.FuelPolicyHistoryService. */
class ExportExpenseReportIdFuelPolicyHistory(private val stub: FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub) : ExportExpenseReportIdSink {
    override suspend fun hand(rec: ExportExpenseReportIdRecord) {
        val request = revertFuelPolicyRequest {
            this.details = RevertFuelPolicyRequestKt.details { this.authorEmail = rec.notes }
        }
        RpcBoundary.run("acme.fuel.v1.FuelPolicyHistoryService/RevertFuelPolicy") { stub.revertFuelPolicy(request) }
    }
}

/** Hands records to acme.driverapp.v1.DriverHomeReportingService. */
class ExportExpenseReportIdDriverHomeReporting(private val stub: DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineStub) : ExportExpenseReportIdSink {
    override suspend fun hand(rec: ExportExpenseReportIdRecord) {
        val request = deleteDriverHomeReportRequest {
            this.reason = rec.correlationId
            this.driverHomeId = rec.status
        }
        stub.deleteDriverHomeReport(request)
    }
}

/** Sends records to acme.performance.v1.ReviewQueryService. */
class ExportExpenseReportIdReviewQuery(private val stub: ReviewQueryServiceGrpcKt.ReviewQueryServiceCoroutineStub) : ExportExpenseReportIdSink {
    override suspend fun hand(rec: ExportExpenseReportIdRecord) {
        val request = findReviewsRequest {
            this.filter = FindReviewsRequestKt.filter { this.query = rec.correlationId }
        }
        stub.findReviews(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class ExportExpenseReportIdLogOnly : ExportExpenseReportIdSink {
    override suspend fun hand(rec: ExportExpenseReportIdRecord) {
        LoggerFactory.getLogger("ExportExpenseReportIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchExportExpenseReportId(kind: String, rec: ExportExpenseReportIdRecord, channel: ManagedChannel) {
    var write: suspend (ExportExpenseReportIdRecord) -> Unit = ExportExpenseReportIdLogOnly()::hand
    when (kind) {
        "inspection-approval" -> write = ExportExpenseReportIdInspectionApproval(InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub(channel))::hand
        "fuel-policy-history" -> write = ExportExpenseReportIdFuelPolicyHistory(FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub(channel))::hand
        "driver-home-reporting" -> write = ExportExpenseReportIdDriverHomeReporting(DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineStub(channel))::hand
        "review-query" -> write = ExportExpenseReportIdReviewQuery(ReviewQueryServiceGrpcKt.ReviewQueryServiceCoroutineStub(channel))::hand
    }
    write(rec)
}
