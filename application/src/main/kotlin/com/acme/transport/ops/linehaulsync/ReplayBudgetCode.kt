package com.acme.transport.ops.linehaulsync

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.normalizeAuditEventRequest
import com.acme.broadcasts.v1.BroadcastQueryServiceGrpcKt
import com.acme.broadcasts.v1.BrowseBroadcastsRequestKt
import com.acme.broadcasts.v1.browseBroadcastsRequest
import com.acme.budgeting.v1.BudgetServiceGrpcKt
import com.acme.budgeting.v1.createBudgetRequest
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.deleteMetricSeriesRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.budgeting.v1.BudgetService/CreateBudget and hands what it keeps to the sink of its kind. */
suspend fun replayBudgetCode(channel: ManagedChannel) {
    val reader = BudgetServiceGrpcKt.BudgetServiceCoroutineStub(channel)
    val current = reader.createBudget(createBudgetRequest { this.requestId = "default-replay-budget-code" })
    val record = ReplayBudgetCodeRecord(
        currencyCode = normalize(current.budget.currencyCode),
        message = normalize(current.result.message),
    )
    deliverReplayBudgetCode("broadcast-query", record, channel)
}

/** Where replayBudgetCode hands each record. */
interface ReplayBudgetCodeWriter {
    suspend fun relay(rec: ReplayBudgetCodeRecord)
}

/** What replayBudgetCode keeps of each CreateBudget response. */
data class ReplayBudgetCodeRecord(val currencyCode: String, val message: String)

/** Passes records to acme.audit.v1.AuditEventValidationService. */
class ReplayBudgetCodeAuditEventValidation(private val stub: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub) : ReplayBudgetCodeWriter {
    override suspend fun relay(rec: ReplayBudgetCodeRecord) {
        val request = normalizeAuditEventRequest {
            this.auditEvent = auditEvent { this.kind = rec.currencyCode }
            this.reason = rec.message
        }
        stub.normalizeAuditEvent(request)
    }
}

/** Hands records to acme.metrics.v1.MetricSeriesService. */
class ReplayBudgetCodeMetricSeries(private val stub: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub) : ReplayBudgetCodeWriter {
    override suspend fun relay(rec: ReplayBudgetCodeRecord) {
        val request = deleteMetricSeriesRequest {
            this.etag = rec.message
            this.metricSeriesId = rec.currencyCode
        }
        stub.deleteMetricSeries(request)
    }
}

/** Sends records to acme.broadcasts.v1.BroadcastQueryService. */
class ReplayBudgetCodeBroadcastQuery(private val stub: BroadcastQueryServiceGrpcKt.BroadcastQueryServiceCoroutineStub) : ReplayBudgetCodeWriter {
    override suspend fun relay(rec: ReplayBudgetCodeRecord) {
        val request = browseBroadcastsRequest {
            this.filter = BrowseBroadcastsRequestKt.filter { this.query = rec.currencyCode }
        }
        stub.browseBroadcasts(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class ReplayBudgetCodeLogOnly : ReplayBudgetCodeWriter {
    override suspend fun relay(rec: ReplayBudgetCodeRecord) {
        LoggerFactory.getLogger("ReplayBudgetCodeRecord").atInfo().addKeyValue("currencyCode", rec.currencyCode).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverReplayBudgetCode(kind: String, rec: ReplayBudgetCodeRecord, channel: ManagedChannel) {
    var sink: ReplayBudgetCodeWriter = ReplayBudgetCodeLogOnly()
    when (kind) {
        "audit-event-validation" -> sink = ReplayBudgetCodeAuditEventValidation(AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel))
        "metric-series" -> sink = ReplayBudgetCodeMetricSeries(MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub(channel))
        "broadcast-query" -> sink = ReplayBudgetCodeBroadcastQuery(BroadcastQueryServiceGrpcKt.BroadcastQueryServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
