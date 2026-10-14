package com.acme.transport.ops.emissionspublisher

import com.acme.fuel.v1.BrowseFuelTransactionsRequestKt
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.browseFuelTransactionsRequest
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.SimulatePartRequestKt
import com.acme.maintenance.v1.part
import com.acme.maintenance.v1.simulatePartRequest
import com.acme.ratelimiting.v1.RateLimitServiceGrpcKt
import com.acme.ratelimiting.v1.deleteRateLimitRequest
import com.acme.telematics.v1.TelemetryReadingQueryServiceGrpcKt
import com.acme.telematics.v1.browseTelemetryReadingsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TelemetryReadingQueryService/BrowseTelemetryReadings and hands what it keeps to the sink of its kind. */
suspend fun forwardTelemetryReadingsToken(channel: ManagedChannel) {
    val upstream = TelemetryReadingQueryServiceGrpcKt.TelemetryReadingQueryServiceCoroutineStub(channel)
    val got = RpcBoundary.read { upstream.browseTelemetryReadings(browseTelemetryReadingsRequest { this.orderBy = "primary-forward-telemetry-readings-token" }) }
    val entry = ForwardTelemetryReadingsTokenRecord(
        nextPageToken = got.pageInfo.nextPageToken,
        status = got.result.status,
    )
    val sinks = mapOf<String, ForwardTelemetryReadingsTokenDestination>(
        "part-calculation" to ForwardTelemetryReadingsTokenPartCalculation(PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub(channel)),
        "rate-limit" to ForwardTelemetryReadingsTokenRateLimit(RateLimitServiceGrpcKt.RateLimitServiceCoroutineStub(channel)),
        "fuel-transaction-query" to ForwardTelemetryReadingsTokenFuelTransactionQuery(FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub(channel)),
    )
    routeForwardTelemetryReadingsToken(sinks, "part-calculation", entry)
}

/** Where forwardTelemetryReadingsToken hands each record. */
interface ForwardTelemetryReadingsTokenDestination {
    suspend fun transmit(rec: ForwardTelemetryReadingsTokenRecord)
}

/** What forwardTelemetryReadingsToken keeps of each BrowseTelemetryReadings response. */
data class ForwardTelemetryReadingsTokenRecord(val nextPageToken: String, val status: String)

/** Writes records to acme.maintenance.v1.PartCalculationService. */
class ForwardTelemetryReadingsTokenPartCalculation(private val stub: PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub) : ForwardTelemetryReadingsTokenDestination {
    override suspend fun transmit(rec: ForwardTelemetryReadingsTokenRecord) {
        val request = simulatePartRequest {
            this.part = part { this.partId = rec.status }
            this.details = SimulatePartRequestKt.details { this.currencyCode = rec.nextPageToken }
        }
        RpcBoundary.run("acme.maintenance.v1.PartCalculationService/SimulatePart") { stub.simulatePart(request) }
    }
}

/** Passes records to acme.ratelimiting.v1.RateLimitService. */
class ForwardTelemetryReadingsTokenRateLimit(private val stub: RateLimitServiceGrpcKt.RateLimitServiceCoroutineStub) : ForwardTelemetryReadingsTokenDestination {
    override suspend fun transmit(rec: ForwardTelemetryReadingsTokenRecord) {
        stub.deleteRateLimit(deleteRateLimitRequest { this.reason = rec.nextPageToken })
    }
}

/** Sends records to acme.fuel.v1.FuelTransactionQueryService. */
class ForwardTelemetryReadingsTokenFuelTransactionQuery(private val stub: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub) : ForwardTelemetryReadingsTokenDestination {
    override suspend fun transmit(rec: ForwardTelemetryReadingsTokenRecord) {
        val request = browseFuelTransactionsRequest {
            this.filter = BrowseFuelTransactionsRequestKt.filter { this.region = rec.nextPageToken; this.query = rec.status }
        }
        stub.browseFuelTransactions(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeForwardTelemetryReadingsToken(sinks: Map<String, ForwardTelemetryReadingsTokenDestination>, kind: String, rec: ForwardTelemetryReadingsTokenRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.transmit(rec)
}
