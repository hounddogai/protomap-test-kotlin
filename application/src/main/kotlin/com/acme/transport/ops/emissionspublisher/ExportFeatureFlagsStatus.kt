package com.acme.transport.ops.emissionspublisher

import com.acme.boxoffice.v1.BoxOfficeSaleQueryServiceGrpcKt
import com.acme.boxoffice.v1.lookupBoxOfficeSaleRequest
import com.acme.customers.v1.ConsentRecordReportingServiceGrpcKt
import com.acme.customers.v1.fetchConsentRecordReportRequest
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.compareDutyStatusOptionsRequest
import com.acme.drivers.v1.dutyStatus
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.browseFeatureFlagsRequest
import com.acme.fuel.v1.FindFuelTransactionsRequestKt
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.findFuelTransactionsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.featureflags.v1.FeatureFlagQueryService/BrowseFeatureFlags and hands what it keeps to the sink of its kind. */
suspend fun exportFeatureFlagsStatus(channel: ManagedChannel) {
    val upstream = FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub(channel)
    val current = upstream.browseFeatureFlags(browseFeatureFlagsRequest { this.orderBy = "live-export-feature-flags-status" })
    val rec = ExportFeatureFlagsStatusRecord(
        status = current.result.status,
        message = current.result.message,
    )
    actOnExportFeatureFlagsStatus("consent-record-reporting", rec, channel)
}

/** Where exportFeatureFlagsStatus hands each record. */
interface ExportFeatureFlagsStatusWriter {
    suspend fun transmit(rec: ExportFeatureFlagsStatusRecord)
}

/** What exportFeatureFlagsStatus keeps of each BrowseFeatureFlags response. */
data class ExportFeatureFlagsStatusRecord(val status: String, val message: String)

/** Sends records to acme.boxoffice.v1.BoxOfficeSaleQueryService. */
class ExportFeatureFlagsStatusBoxOfficeSaleQuery(private val stub: BoxOfficeSaleQueryServiceGrpcKt.BoxOfficeSaleQueryServiceCoroutineStub) : ExportFeatureFlagsStatusWriter {
    override suspend fun transmit(rec: ExportFeatureFlagsStatusRecord) {
        stub.lookupBoxOfficeSale(lookupBoxOfficeSaleRequest { this.boxOfficeSaleId = rec.status })
    }
}

/** Writes records to acme.fuel.v1.FuelTransactionQueryService. */
class ExportFeatureFlagsStatusFuelTransactionQuery(private val stub: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub) : ExportFeatureFlagsStatusWriter {
    override suspend fun transmit(rec: ExportFeatureFlagsStatusRecord) {
        val request = findFuelTransactionsRequest {
            this.filter = FindFuelTransactionsRequestKt.filter { this.query = rec.status }
        }
        stub.findFuelTransactions(request)
    }
}

/** Writes records to acme.drivers.v1.DutyStatusCalculationService. */
class ExportFeatureFlagsStatusDutyStatusCalculation(private val stub: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub) : ExportFeatureFlagsStatusWriter {
    override suspend fun transmit(rec: ExportFeatureFlagsStatusRecord) {
        val request = compareDutyStatusOptionsRequest {
            this.dutyStatusId = rec.status
            this.dutyStatus = dutyStatus { this.dutyStatusId = rec.message }
        }
        RpcBoundary.run("acme.drivers.v1.DutyStatusCalculationService/CompareDutyStatusOptions") { stub.compareDutyStatusOptions(request) }
    }
}

/** Sends records to acme.customers.v1.ConsentRecordReportingService. */
class ExportFeatureFlagsStatusConsentRecordReporting(private val stub: ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub) : ExportFeatureFlagsStatusWriter {
    override suspend fun transmit(rec: ExportFeatureFlagsStatusRecord) {
        stub.fetchConsentRecordReport(fetchConsentRecordReportRequest { this.consentRecordId = rec.status })
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnExportFeatureFlagsStatus(kind: String, rec: ExportFeatureFlagsStatusRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "box-office-sale-query" to { ExportFeatureFlagsStatusBoxOfficeSaleQuery(BoxOfficeSaleQueryServiceGrpcKt.BoxOfficeSaleQueryServiceCoroutineStub(channel)).transmit(rec) },
        "fuel-transaction-query" to { ExportFeatureFlagsStatusFuelTransactionQuery(FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub(channel)).transmit(rec) },
        "duty-status-calculation" to { ExportFeatureFlagsStatusDutyStatusCalculation(DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub(channel)).transmit(rec) },
        "consent-record-reporting" to { ExportFeatureFlagsStatusConsentRecordReporting(ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub(channel)).transmit(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
