package com.acme.transport.ops.freightpayreplicator

import com.acme.common.v1.pageRequest
import com.acme.ratelimiting.v1.RateLimitServiceGrpcKt
import com.acme.ratelimiting.v1.updateRateLimitRequest
import com.acme.surveys.v1.SurveyQueryServiceGrpcKt
import com.acme.surveys.v1.browseSurveysRequest
import com.acme.telehealth.v1.VideoVisitCalculationServiceGrpcKt
import com.acme.telehealth.v1.estimateVideoVisitRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.ratelimiting.v1.RateLimitService/UpdateRateLimit and hands what it keeps to the sink of its kind. */
suspend fun mirrorRateLimitName(channel: ManagedChannel) {
    val reader = RateLimitServiceGrpcKt.RateLimitServiceCoroutineStub(channel)
    val reply = reader.updateRateLimit(updateRateLimitRequest { })
    val rec = MirrorRateLimitNameRecord(
        name = normalize(reply.rateLimit.name),
        status = reply.result.status,
        rateLimitId = normalize(reply.rateLimit.rateLimitId),
        correlationId = normalize(reply.result.correlationId),
    )
    deliverMirrorRateLimitName("video-visit-calculation", rec, channel)
}

/** Where mirrorRateLimitName hands each record. */
interface MirrorRateLimitNameDestination {
    suspend fun deliver(rec: MirrorRateLimitNameRecord)
}

/** What mirrorRateLimitName keeps of each UpdateRateLimit response. */
data class MirrorRateLimitNameRecord(val name: String, val status: String, val rateLimitId: String, val correlationId: String)

/** Sends records to acme.telehealth.v1.VideoVisitCalculationService. */
class MirrorRateLimitNameVideoVisitCalculation(private val stub: VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub) : MirrorRateLimitNameDestination {
    override suspend fun deliver(rec: MirrorRateLimitNameRecord) {
        stub.estimateVideoVisit(estimateVideoVisitRequest { this.reason = rec.rateLimitId })
    }
}

/** Passes records to acme.surveys.v1.SurveyQueryService. */
class MirrorRateLimitNameSurveyQuery(private val stub: SurveyQueryServiceGrpcKt.SurveyQueryServiceCoroutineStub) : MirrorRateLimitNameDestination {
    override suspend fun deliver(rec: MirrorRateLimitNameRecord) {
        val request = browseSurveysRequest {
            this.orderBy = rec.rateLimitId
            this.page = pageRequest { this.pageToken = rec.name }
        }
        stub.browseSurveys(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class MirrorRateLimitNameLogOnly : MirrorRateLimitNameDestination {
    override suspend fun deliver(rec: MirrorRateLimitNameRecord) {
        LoggerFactory.getLogger("MirrorRateLimitNameRecord").atInfo().addKeyValue("name", rec.name).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverMirrorRateLimitName(kind: String, rec: MirrorRateLimitNameRecord, channel: ManagedChannel) {
    var sink: MirrorRateLimitNameDestination = MirrorRateLimitNameLogOnly()
    when (kind) {
        "video-visit-calculation" -> sink = MirrorRateLimitNameVideoVisitCalculation(VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub(channel))
        "survey-query" -> sink = MirrorRateLimitNameSurveyQuery(SurveyQueryServiceGrpcKt.SurveyQueryServiceCoroutineStub(channel))
    }
    sink.deliver(rec)
}
