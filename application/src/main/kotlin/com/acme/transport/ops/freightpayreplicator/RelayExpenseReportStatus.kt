package com.acme.transport.ops.freightpayreplicator

import com.acme.budgeting.v1.BudgetCalculationServiceGrpcKt
import com.acme.budgeting.v1.recalculateBudgetsRequest
import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.previewDocumentRequest
import com.acme.expenses.v1.ExpenseReportCalculationServiceGrpcKt
import com.acme.expenses.v1.simulateExpenseReportRequest
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.mapTelemetryReadingImportColumnsRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.expenses.v1.ExpenseReportCalculationService/SimulateExpenseReport and hands what it keeps to the sink of its kind. */
suspend fun relayExpenseReportStatus(channel: ManagedChannel) {
    val reader = ExpenseReportCalculationServiceGrpcKt.ExpenseReportCalculationServiceCoroutineStub(channel)
    val current = reader.simulateExpenseReport(simulateExpenseReportRequest { this.reason = "primary-relay-expense-report-status" })
    val record = RelayExpenseReportStatusRecord(
        status = normalize(current.result.status),
        currencyCode = current.breakdown.currencyCode,
        notes = current.expenseReport.notes,
    )
    val sinks = mapOf<String, RelayExpenseReportStatusDestination>(
        "telemetry-reading-import" to RelayExpenseReportStatusTelemetryReadingImport(TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub(channel)),
        "document-validation" to RelayExpenseReportStatusDocumentValidation(DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub(channel)),
        "budget-calculation" to RelayExpenseReportStatusBudgetCalculation(BudgetCalculationServiceGrpcKt.BudgetCalculationServiceCoroutineStub(channel)),
    )
    routeRelayExpenseReportStatus(sinks, "budget-calculation", record)
}

/** Where relayExpenseReportStatus hands each record. */
interface RelayExpenseReportStatusDestination {
    suspend fun deliver(rec: RelayExpenseReportStatusRecord)
}

/** What relayExpenseReportStatus keeps of each SimulateExpenseReport response. */
data class RelayExpenseReportStatusRecord(val status: String, val currencyCode: String, val notes: String)

/** Hands records to acme.telematics.v1.TelemetryReadingImportService. */
class RelayExpenseReportStatusTelemetryReadingImport(private val stub: TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub) : RelayExpenseReportStatusDestination {
    override suspend fun deliver(rec: RelayExpenseReportStatusRecord) {
        val request = mapTelemetryReadingImportColumnsRequest {
            this.telemetryReading = telemetryReading { this.correlationId = rec.currencyCode; this.telemetryReadingId = rec.status }
            this.telemetryReadingId = rec.notes
        }
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingImportService/MapTelemetryReadingImportColumns") { stub.mapTelemetryReadingImportColumns(request) }
    }
}

/** Writes records to acme.documents.v1.DocumentValidationService. */
class RelayExpenseReportStatusDocumentValidation(private val stub: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub) : RelayExpenseReportStatusDestination {
    override suspend fun deliver(rec: RelayExpenseReportStatusRecord) {
        stub.previewDocument(previewDocumentRequest { this.reason = rec.notes })
    }
}

/** Writes records to acme.budgeting.v1.BudgetCalculationService. */
class RelayExpenseReportStatusBudgetCalculation(private val stub: BudgetCalculationServiceGrpcKt.BudgetCalculationServiceCoroutineStub) : RelayExpenseReportStatusDestination {
    override suspend fun deliver(rec: RelayExpenseReportStatusRecord) {
        stub.recalculateBudgets(recalculateBudgetsRequest { this.reason = rec.notes })
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeRelayExpenseReportStatus(sinks: Map<String, RelayExpenseReportStatusDestination>, kind: String, rec: RelayExpenseReportStatusRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.deliver(rec)
}
