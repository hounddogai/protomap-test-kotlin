package com.acme.transport.ops.freightpayreplicator

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.lintAuditEventsRequest
import com.acme.driverapp.v1.DriverHomeQueryServiceGrpcKt
import com.acme.driverapp.v1.summarizeDriverHomesRequest
import com.acme.fuel.v1.FuelPolicyReportingServiceGrpcKt
import com.acme.fuel.v1.fetchFuelPolicyReportRequest
import com.acme.shows.v1.ShowCalculationServiceGrpcKt
import com.acme.shows.v1.calculateShowRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fuel.v1.FuelPolicyReportingService/FetchFuelPolicyReport and hands what it keeps to the sink of its kind. */
suspend fun copyFuelPolicyReportStatus(channel: ManagedChannel) {
    val reader = FuelPolicyReportingServiceGrpcKt.FuelPolicyReportingServiceCoroutineStub(channel)
    val got = reader.fetchFuelPolicyReport(fetchFuelPolicyReportRequest { this.fuelPolicyId = "current-copy-fuel-policy-report-status" })
    val row = CopyFuelPolicyReportStatusRecord(
        status = got.result.status,
        fuelPolicyId = got.fuelPolicy.fuelPolicyId,
        correlationId = got.result.correlationId,
    )
    deliverCopyFuelPolicyReportStatus("driver-home-query", row, channel)
}

/** Where copyFuelPolicyReportStatus hands each record. */
interface CopyFuelPolicyReportStatusSink {
    suspend fun hand(rec: CopyFuelPolicyReportStatusRecord)
}

/** What copyFuelPolicyReportStatus keeps of each FetchFuelPolicyReport response. */
data class CopyFuelPolicyReportStatusRecord(val status: String, val fuelPolicyId: String, val correlationId: String)

/** Passes records to acme.audit.v1.AuditEventValidationService. */
class CopyFuelPolicyReportStatusAuditEventValidation(private val stub: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub) : CopyFuelPolicyReportStatusSink {
    override suspend fun hand(rec: CopyFuelPolicyReportStatusRecord) {
        stub.lintAuditEvents(lintAuditEventsRequest { this.reason = rec.status })
    }
}

/** Sends records to acme.driverapp.v1.DriverHomeQueryService. */
class CopyFuelPolicyReportStatusDriverHomeQuery(private val stub: DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub) : CopyFuelPolicyReportStatusSink {
    override suspend fun hand(rec: CopyFuelPolicyReportStatusRecord) {
        stub.summarizeDriverHomes(summarizeDriverHomesRequest { this.driverHomeId = rec.status })
    }
}

/** Hands records to acme.shows.v1.ShowCalculationService. */
class CopyFuelPolicyReportStatusShowCalculation(private val stub: ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub) : CopyFuelPolicyReportStatusSink {
    override suspend fun hand(rec: CopyFuelPolicyReportStatusRecord) {
        RpcBoundary.run("acme.shows.v1.ShowCalculationService/CalculateShow") { stub.calculateShow(calculateShowRequest { this.showId = rec.correlationId }) }
    }
}

/** Only logs, for kinds no service takes yet. */
class CopyFuelPolicyReportStatusLogOnly : CopyFuelPolicyReportStatusSink {
    override suspend fun hand(rec: CopyFuelPolicyReportStatusRecord) {
        LoggerFactory.getLogger("CopyFuelPolicyReportStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverCopyFuelPolicyReportStatus(kind: String, rec: CopyFuelPolicyReportStatusRecord, channel: ManagedChannel) {
    var sink: CopyFuelPolicyReportStatusSink = CopyFuelPolicyReportStatusLogOnly()
    when (kind) {
        "audit-event-validation" -> sink = CopyFuelPolicyReportStatusAuditEventValidation(AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel))
        "driver-home-query" -> sink = CopyFuelPolicyReportStatusDriverHomeQuery(DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub(channel))
        "show-calculation" -> sink = CopyFuelPolicyReportStatusShowCalculation(ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
