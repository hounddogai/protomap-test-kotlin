package com.acme.transport.ops.emissionspublisher

import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.browseDocumentsRequest
import com.acme.driverapp.v1.DriverHomeQueryServiceGrpcKt
import com.acme.driverapp.v1.SummarizeDriverHomesRequestKt
import com.acme.driverapp.v1.summarizeDriverHomesRequest
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionLifecycleServiceGrpcKt
import com.acme.fuel.v1.getFuelPolicyRulesRequest
import com.acme.fuel.v1.getFuelTransactionLifecycleRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.documents.v1.DocumentQueryService/BrowseDocuments and hands what it keeps to the sink of its kind. */
suspend fun exportDocumentsStatus(channel: ManagedChannel) {
    val origin = DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub(channel)
    val current = origin.browseDocuments(browseDocumentsRequest { this.orderBy = "main-export-documents-status" })
    val record = ExportDocumentsStatusRecord(
        status = normalize(current.result.status),
        correlationId = current.result.correlationId,
        message = current.result.message,
    )
    val sinks = listOf<ExportDocumentsStatusWriter>(
        ExportDocumentsStatusFuelTransactionLifecycle(FuelTransactionLifecycleServiceGrpcKt.FuelTransactionLifecycleServiceCoroutineStub(channel)),
        ExportDocumentsStatusDriverHomeQuery(DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub(channel)),
        ExportDocumentsStatusFuelPolicyValidation(FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub(channel)),
    )
    sinks[2].hand(record)
}

/** Where exportDocumentsStatus hands each record. */
interface ExportDocumentsStatusWriter {
    suspend fun hand(rec: ExportDocumentsStatusRecord)
}

/** What exportDocumentsStatus keeps of each BrowseDocuments response. */
data class ExportDocumentsStatusRecord(val status: String, val correlationId: String, val message: String)

/** Hands records to acme.fuel.v1.FuelTransactionLifecycleService. */
class ExportDocumentsStatusFuelTransactionLifecycle(private val stub: FuelTransactionLifecycleServiceGrpcKt.FuelTransactionLifecycleServiceCoroutineStub) : ExportDocumentsStatusWriter {
    override suspend fun hand(rec: ExportDocumentsStatusRecord) {
        RpcBoundary.run("acme.fuel.v1.FuelTransactionLifecycleService/GetFuelTransactionLifecycle") { stub.getFuelTransactionLifecycle(getFuelTransactionLifecycleRequest { this.fuelTransactionId = rec.message }) }
    }
}

/** Passes records to acme.driverapp.v1.DriverHomeQueryService. */
class ExportDocumentsStatusDriverHomeQuery(private val stub: DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub) : ExportDocumentsStatusWriter {
    override suspend fun hand(rec: ExportDocumentsStatusRecord) {
        val request = summarizeDriverHomesRequest {
            this.details = SummarizeDriverHomesRequestKt.details { this.cron = rec.message; this.format = rec.status }
            this.reason = rec.correlationId
        }
        stub.summarizeDriverHomes(request)
    }
}

/** Passes records to acme.fuel.v1.FuelPolicyValidationService. */
class ExportDocumentsStatusFuelPolicyValidation(private val stub: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub) : ExportDocumentsStatusWriter {
    override suspend fun hand(rec: ExportDocumentsStatusRecord) {
        RpcBoundary.run("acme.fuel.v1.FuelPolicyValidationService/GetFuelPolicyRules") { stub.getFuelPolicyRules(getFuelPolicyRulesRequest { this.fuelPolicyId = rec.message }) }
    }
}
