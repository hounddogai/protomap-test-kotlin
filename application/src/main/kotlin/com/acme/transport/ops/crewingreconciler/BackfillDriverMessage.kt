package com.acme.transport.ops.crewingreconciler

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.FindAccessPoliciesRequestKt
import com.acme.authorization.v1.findAccessPoliciesRequest
import com.acme.drivers.v1.DriverLifecycleServiceGrpcKt
import com.acme.drivers.v1.terminateDriverRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.ScheduleYardMoveReportRequestKt
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.scheduleYardMoveReportRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.drivers.v1.DriverLifecycleService/TerminateDriver and hands what it keeps to the sink of its kind. */
suspend fun backfillDriverMessage(channel: ManagedChannel) {
    val upstream = DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub(channel)
    val reply = upstream.terminateDriver(terminateDriverRequest { this.reason = "default-backfill-driver-message" })
    val row = BackfillDriverMessageRecord(
        message = normalize(reply.result.message),
        correlationId = reply.result.correlationId,
    )
    deliverBackfillDriverMessage("yard-move-reporting", row, channel)
}

/** Where backfillDriverMessage hands each record. */
interface BackfillDriverMessageWriter {
    suspend fun forward(rec: BackfillDriverMessageRecord)
}

/** What backfillDriverMessage keeps of each TerminateDriver response. */
data class BackfillDriverMessageRecord(val message: String, val correlationId: String)

/** Writes records to acme.authorization.v1.AccessPolicyQueryService. */
class BackfillDriverMessageAccessPolicyQuery(private val stub: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub) : BackfillDriverMessageWriter {
    override suspend fun forward(rec: BackfillDriverMessageRecord) {
        val request = findAccessPoliciesRequest {
            this.filter = FindAccessPoliciesRequestKt.filter { this.region = rec.message; this.query = rec.correlationId }
        }
        stub.findAccessPolicies(request)
    }
}

/** Hands records to acme.yards.v1.YardMoveReportingService. */
class BackfillDriverMessageYardMoveReporting(private val stub: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub) : BackfillDriverMessageWriter {
    override suspend fun forward(rec: BackfillDriverMessageRecord) {
        val request = scheduleYardMoveReportRequest {
            this.details = ScheduleYardMoveReportRequestKt.details { this.format = rec.correlationId; this.cron = rec.message }
        }
        RpcBoundary.run("acme.yards.v1.YardMoveReportingService/ScheduleYardMoveReport") { stub.scheduleYardMoveReport(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class BackfillDriverMessageLogOnly : BackfillDriverMessageWriter {
    override suspend fun forward(rec: BackfillDriverMessageRecord) {
        LoggerFactory.getLogger("BackfillDriverMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverBackfillDriverMessage(kind: String, rec: BackfillDriverMessageRecord, channel: ManagedChannel) {
    var sink: BackfillDriverMessageWriter = BackfillDriverMessageLogOnly()
    when (kind) {
        "access-policy-query" -> sink = BackfillDriverMessageAccessPolicyQuery(AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub(channel))
        "yard-move-reporting" -> sink = BackfillDriverMessageYardMoveReporting(YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
