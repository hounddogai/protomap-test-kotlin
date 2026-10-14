package com.acme.transport.ops.linehaulimporter

import com.acme.common.v1.pageRequest
import com.acme.concessions.v1.ConcessionStandQueryServiceGrpcKt
import com.acme.concessions.v1.lookupConcessionStandRequest
import com.acme.fanportal.v1.FanSessionServiceGrpcKt
import com.acme.fanportal.v1.SearchFanSessionsRequestKt
import com.acme.fanportal.v1.searchFanSessionsRequest
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.SummarizeFuelTransactionsRequestKt
import com.acme.fuel.v1.summarizeFuelTransactionsRequest
import com.acme.telematics.v1.TelemetryReadingWorkflowServiceGrpcKt
import com.acme.telematics.v1.retryTelemetryReadingWorkflowRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.concessions.v1.ConcessionStandQueryService/LookupConcessionStand and hands what it keeps to the sink of its kind. */
suspend fun pushConcessionStandStatus(channel: ManagedChannel) {
    val source = ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub(channel)
    val found = RpcBoundary.read { source.lookupConcessionStand(lookupConcessionStandRequest { this.concessionStandId = "default-push-concession-stand-status" }) }
    val rec = PushConcessionStandStatusRecord(
        status = found.result.status,
        name = found.concessionStand.name,
        concessionStandId = normalize(found.concessionStand.concessionStandId),
        code = found.concessionStand.code,
    )
    val sinks = mapOf<String, PushConcessionStandStatusWriter>(
        "fan-session" to PushConcessionStandStatusFanSession(FanSessionServiceGrpcKt.FanSessionServiceCoroutineStub(channel)),
        "telemetry-reading-workflow" to PushConcessionStandStatusTelemetryReadingWorkflow(TelemetryReadingWorkflowServiceGrpcKt.TelemetryReadingWorkflowServiceCoroutineStub(channel)),
        "fuel-transaction-query" to PushConcessionStandStatusFuelTransactionQuery(FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "telemetry-reading-workflow") continue
        sinks[kind]?.forward(rec)
    }
}

/** Where pushConcessionStandStatus hands each record. */
interface PushConcessionStandStatusWriter {
    suspend fun forward(rec: PushConcessionStandStatusRecord)
}

/** What pushConcessionStandStatus keeps of each LookupConcessionStand response. */
data class PushConcessionStandStatusRecord(val status: String, val name: String, val concessionStandId: String, val code: String)

/** Passes records to acme.fanportal.v1.FanSessionService. */
class PushConcessionStandStatusFanSession(private val stub: FanSessionServiceGrpcKt.FanSessionServiceCoroutineStub) : PushConcessionStandStatusWriter {
    override suspend fun forward(rec: PushConcessionStandStatusRecord) {
        val request = searchFanSessionsRequest {
            this.filter = SearchFanSessionsRequestKt.filter { this.region = rec.concessionStandId }
            this.page = pageRequest { this.pageToken = rec.code }
        }
        stub.searchFanSessions(request)
    }
}

/** Passes records to acme.telematics.v1.TelemetryReadingWorkflowService. */
class PushConcessionStandStatusTelemetryReadingWorkflow(private val stub: TelemetryReadingWorkflowServiceGrpcKt.TelemetryReadingWorkflowServiceCoroutineStub) : PushConcessionStandStatusWriter {
    override suspend fun forward(rec: PushConcessionStandStatusRecord) {
        val request = retryTelemetryReadingWorkflowRequest {
            this.telemetryReading = telemetryReading { this.source = rec.concessionStandId; this.kind = rec.name }
        }
        stub.retryTelemetryReadingWorkflow(request)
    }
}

/** Hands records to acme.fuel.v1.FuelTransactionQueryService. */
class PushConcessionStandStatusFuelTransactionQuery(private val stub: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub) : PushConcessionStandStatusWriter {
    override suspend fun forward(rec: PushConcessionStandStatusRecord) {
        val request = summarizeFuelTransactionsRequest {
            this.details = SummarizeFuelTransactionsRequestKt.details { this.format = rec.status }
            this.reason = rec.code
        }
        RpcBoundary.run("acme.fuel.v1.FuelTransactionQueryService/SummarizeFuelTransactions") { stub.summarizeFuelTransactions(request) }
    }
}
