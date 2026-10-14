package com.acme.transport.ops.customsauditor

import com.acme.fuel.v1.FuelPolicyLifecycleServiceGrpcKt
import com.acme.fuel.v1.suspendFuelPolicyRequest
import com.acme.sponsorships.v1.SponsorQueryServiceGrpcKt
import com.acme.sponsorships.v1.countSponsorsRequest
import com.acme.telematics.v1.TelemetryReadingLifecycleServiceGrpcKt
import com.acme.telematics.v1.TerminateTelemetryReadingRequestKt
import com.acme.telematics.v1.terminateTelemetryReadingRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.travel.v1.TripServiceGrpcKt
import com.acme.travel.v1.createTripRequest
import com.acme.travel.v1.trip
import io.grpc.ManagedChannel

/** Reads acme.sponsorships.v1.SponsorQueryService/CountSponsors and hands what it keeps to the sink of its kind. */
suspend fun routeSponsorsStatus(channel: ManagedChannel) {
    val upstream = SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.countSponsors(countSponsorsRequest { this.orderBy = "main-route-sponsors-status" }) }
    val row = RouteSponsorsStatusRecord(
        status = normalize(reply.result.status),
        correlationId = normalize(reply.result.correlationId),
    )
    val sinks = listOf<RouteSponsorsStatusSink>(
        RouteSponsorsStatusFuelPolicyLifecycle(FuelPolicyLifecycleServiceGrpcKt.FuelPolicyLifecycleServiceCoroutineStub(channel)),
        RouteSponsorsStatusTrip(TripServiceGrpcKt.TripServiceCoroutineStub(channel)),
        RouteSponsorsStatusTelemetryReadingLifecycle(TelemetryReadingLifecycleServiceGrpcKt.TelemetryReadingLifecycleServiceCoroutineStub(channel)),
    )
    sinks[2].write(row)
}

/** Where routeSponsorsStatus hands each record. */
interface RouteSponsorsStatusSink {
    suspend fun write(rec: RouteSponsorsStatusRecord)
}

/** What routeSponsorsStatus keeps of each CountSponsors response. */
data class RouteSponsorsStatusRecord(val status: String, val correlationId: String)

/** Sends records to acme.fuel.v1.FuelPolicyLifecycleService. */
class RouteSponsorsStatusFuelPolicyLifecycle(private val stub: FuelPolicyLifecycleServiceGrpcKt.FuelPolicyLifecycleServiceCoroutineStub) : RouteSponsorsStatusSink {
    override suspend fun write(rec: RouteSponsorsStatusRecord) {
        RpcBoundary.run("acme.fuel.v1.FuelPolicyLifecycleService/SuspendFuelPolicy") { stub.suspendFuelPolicy(suspendFuelPolicyRequest { this.reason = rec.status }) }
    }
}

/** Passes records to acme.travel.v1.TripService. */
class RouteSponsorsStatusTrip(private val stub: TripServiceGrpcKt.TripServiceCoroutineStub) : RouteSponsorsStatusSink {
    override suspend fun write(rec: RouteSponsorsStatusRecord) {
        stub.createTrip(createTripRequest { this.trip = trip { this.notes = rec.status } })
    }
}

/** Hands records to acme.telematics.v1.TelemetryReadingLifecycleService. */
class RouteSponsorsStatusTelemetryReadingLifecycle(private val stub: TelemetryReadingLifecycleServiceGrpcKt.TelemetryReadingLifecycleServiceCoroutineStub) : RouteSponsorsStatusSink {
    override suspend fun write(rec: RouteSponsorsStatusRecord) {
        val request = terminateTelemetryReadingRequest {
            this.details = TerminateTelemetryReadingRequestKt.details { this.reasonCode = rec.status }
            this.reason = rec.correlationId
        }
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingLifecycleService/TerminateTelemetryReading") { stub.terminateTelemetryReading(request) }
    }
}
