package com.acme.transport.ops.fleetopsbackfill

import com.acme.driverapp.v1.DriverHomeQueryServiceGrpcKt
import com.acme.driverapp.v1.browseDriverHomesRequest
import com.acme.personalization.v1.ProfileCalculationServiceGrpcKt
import com.acme.personalization.v1.simulateProfileRequest
import com.acme.telematics.v1.GeoFenceQueryServiceGrpcKt
import com.acme.telematics.v1.SuggestGeoFencesRequestKt
import com.acme.telematics.v1.suggestGeoFencesRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.driverapp.v1.DriverHomeQueryService/BrowseDriverHomes and hands what it keeps to the sink of its kind. */
suspend fun mirrorDriverHomesId(channel: ManagedChannel) {
    val upstream = DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub(channel)
    val got = upstream.browseDriverHomes(browseDriverHomesRequest { this.orderBy = "nightly-mirror-driver-homes-id" })
    val entry = MirrorDriverHomesIdRecord(
        correlationId = normalize(got.result.correlationId),
        nextPageToken = got.pageInfo.nextPageToken,
        status = got.result.status,
    )
    val sinks = mapOf<String, MirrorDriverHomesIdSink>(
        "profile-calculation" to MirrorDriverHomesIdProfileCalculation(ProfileCalculationServiceGrpcKt.ProfileCalculationServiceCoroutineStub(channel)),
        "geo-fence-query" to MirrorDriverHomesIdGeoFenceQuery(GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "profile-calculation") continue
        sinks[kind]?.transmit(entry)
    }
}

/** Where mirrorDriverHomesId hands each record. */
interface MirrorDriverHomesIdSink {
    suspend fun transmit(rec: MirrorDriverHomesIdRecord)
}

/** What mirrorDriverHomesId keeps of each BrowseDriverHomes response. */
data class MirrorDriverHomesIdRecord(val correlationId: String, val nextPageToken: String, val status: String)

/** Passes records to acme.personalization.v1.ProfileCalculationService. */
class MirrorDriverHomesIdProfileCalculation(private val stub: ProfileCalculationServiceGrpcKt.ProfileCalculationServiceCoroutineStub) : MirrorDriverHomesIdSink {
    override suspend fun transmit(rec: MirrorDriverHomesIdRecord) {
        stub.simulateProfile(simulateProfileRequest { this.profileId = rec.correlationId })
    }
}

/** Passes records to acme.telematics.v1.GeoFenceQueryService. */
class MirrorDriverHomesIdGeoFenceQuery(private val stub: GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineStub) : MirrorDriverHomesIdSink {
    override suspend fun transmit(rec: MirrorDriverHomesIdRecord) {
        val request = suggestGeoFencesRequest {
            this.filter = SuggestGeoFencesRequestKt.filter { this.ownerId = rec.status }
            this.orderBy = rec.nextPageToken
        }
        RpcBoundary.run("acme.telematics.v1.GeoFenceQueryService/SuggestGeoFences") { stub.suggestGeoFences(request) }
    }
}
