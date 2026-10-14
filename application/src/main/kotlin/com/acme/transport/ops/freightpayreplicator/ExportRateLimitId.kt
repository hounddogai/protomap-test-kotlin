package com.acme.transport.ops.freightpayreplicator

import com.acme.maintenance.v1.GeneratePartReportRequestKt
import com.acme.maintenance.v1.PartReportingServiceGrpcKt
import com.acme.maintenance.v1.generatePartReportRequest
import com.acme.performance.v1.ReviewServiceGrpcKt
import com.acme.performance.v1.review
import com.acme.performance.v1.updateReviewRequest
import com.acme.ratelimiting.v1.RateLimitQueryServiceGrpcKt
import com.acme.ratelimiting.v1.lookupRateLimitRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.ratelimiting.v1.RateLimitQueryService/LookupRateLimit and hands what it keeps to the sink of its kind. */
suspend fun exportRateLimitId(channel: ManagedChannel) {
    val upstream = RateLimitQueryServiceGrpcKt.RateLimitQueryServiceCoroutineStub(channel)
    val found = RpcBoundary.read { upstream.lookupRateLimit(lookupRateLimitRequest { this.rateLimitId = "main-export-rate-limit-id" }) }
    val record = ExportRateLimitIdRecord(
        correlationId = normalize(found.result.correlationId),
        description = found.rateLimit.description,
        rateLimitId = found.rateLimit.rateLimitId,
        status = found.result.status,
    )
    actOnExportRateLimitId("review", record, channel)
}

/** Where exportRateLimitId hands each record. */
interface ExportRateLimitIdWriter {
    suspend fun relay(rec: ExportRateLimitIdRecord)
}

/** What exportRateLimitId keeps of each LookupRateLimit response. */
data class ExportRateLimitIdRecord(val correlationId: String, val description: String, val rateLimitId: String, val status: String)

/** Writes records to acme.maintenance.v1.PartReportingService. */
class ExportRateLimitIdPartReporting(private val stub: PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub) : ExportRateLimitIdWriter {
    override suspend fun relay(rec: ExportRateLimitIdRecord) {
        val request = generatePartReportRequest {
            this.details = GeneratePartReportRequestKt.details { this.format = rec.status }
            this.partId = rec.description
        }
        RpcBoundary.run("acme.maintenance.v1.PartReportingService/GeneratePartReport") { stub.generatePartReport(request) }
    }
}

/** Writes records to acme.performance.v1.ReviewService. */
class ExportRateLimitIdReview(private val stub: ReviewServiceGrpcKt.ReviewServiceCoroutineStub) : ExportRateLimitIdWriter {
    override suspend fun relay(rec: ExportRateLimitIdRecord) {
        stub.updateReview(updateReviewRequest { this.review = review { this.notes = rec.status } })
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnExportRateLimitId(kind: String, rec: ExportRateLimitIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "part-reporting" to { ExportRateLimitIdPartReporting(PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub(channel)).relay(rec) },
        "review" to { ExportRateLimitIdReview(ReviewServiceGrpcKt.ReviewServiceCoroutineStub(channel)).relay(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
