package com.acme.transport.ops.terminalsweeper

import com.acme.customers.v1.PreferenceSyncServiceGrpcKt
import com.acme.customers.v1.pullPreferencesRequest
import com.acme.fleet.v1.CheckVehicleGroupEligibilityRequestKt
import com.acme.fleet.v1.InspectionLifecycleServiceGrpcKt
import com.acme.fleet.v1.VehicleGroupValidationServiceGrpcKt
import com.acme.fleet.v1.checkVehicleGroupEligibilityRequest
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.scheduleInspectionExpiryRequest
import com.acme.fleet.v1.vehicleGroup
import com.acme.search.v1.SearchIndexQueryServiceGrpcKt
import com.acme.search.v1.summarizeSearchIndexesRequest
import com.acme.telematics.v1.GeoFenceApprovalServiceGrpcKt
import com.acme.telematics.v1.recallGeoFenceRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.PreferenceSyncService/PullPreferences and hands what it keeps to the sink of its kind. */
suspend fun mirrorPreferencesName(channel: ManagedChannel) {
    val reader = PreferenceSyncServiceGrpcKt.PreferenceSyncServiceCoroutineStub(channel)
    val reply = reader.pullPreferences(pullPreferencesRequest { this.reason = "current-mirror-preferences-name" })
    val record = MirrorPreferencesNameRecord(
        name = normalize(reply.preference.name),
        message = reply.result.message,
    )
    val sinks = listOf<MirrorPreferencesNameDestination>(
        MirrorPreferencesNameInspectionLifecycle(InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub(channel)),
        MirrorPreferencesNameGeoFenceApproval(GeoFenceApprovalServiceGrpcKt.GeoFenceApprovalServiceCoroutineStub(channel)),
        MirrorPreferencesNameVehicleGroupValidation(VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub(channel)),
        MirrorPreferencesNameSearchIndexQuery(SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub(channel)),
    )
    sinks[2].relay(record)
}

/** Where mirrorPreferencesName hands each record. */
interface MirrorPreferencesNameDestination {
    suspend fun relay(rec: MirrorPreferencesNameRecord)
}

/** What mirrorPreferencesName keeps of each PullPreferences response. */
data class MirrorPreferencesNameRecord(val name: String, val message: String)

/** Sends records to acme.fleet.v1.InspectionLifecycleService. */
class MirrorPreferencesNameInspectionLifecycle(private val stub: InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub) : MirrorPreferencesNameDestination {
    override suspend fun relay(rec: MirrorPreferencesNameRecord) {
        val request = scheduleInspectionExpiryRequest {
            this.inspection = inspection { this.subject = rec.name; this.inspectionId = rec.message }
        }
        stub.scheduleInspectionExpiry(request)
    }
}

/** Passes records to acme.telematics.v1.GeoFenceApprovalService. */
class MirrorPreferencesNameGeoFenceApproval(private val stub: GeoFenceApprovalServiceGrpcKt.GeoFenceApprovalServiceCoroutineStub) : MirrorPreferencesNameDestination {
    override suspend fun relay(rec: MirrorPreferencesNameRecord) {
        stub.recallGeoFence(recallGeoFenceRequest { this.geoFenceId = rec.message })
    }
}

/** Sends records to acme.fleet.v1.VehicleGroupValidationService. */
class MirrorPreferencesNameVehicleGroupValidation(private val stub: VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub) : MirrorPreferencesNameDestination {
    override suspend fun relay(rec: MirrorPreferencesNameRecord) {
        val request = checkVehicleGroupEligibilityRequest {
            this.vehicleGroup = vehicleGroup { this.parentId = rec.name }
            this.details = CheckVehicleGroupEligibilityRequestKt.details { this.locale = rec.message }
        }
        RpcBoundary.run("acme.fleet.v1.VehicleGroupValidationService/CheckVehicleGroupEligibility") { stub.checkVehicleGroupEligibility(request) }
    }
}

/** Hands records to acme.search.v1.SearchIndexQueryService. */
class MirrorPreferencesNameSearchIndexQuery(private val stub: SearchIndexQueryServiceGrpcKt.SearchIndexQueryServiceCoroutineStub) : MirrorPreferencesNameDestination {
    override suspend fun relay(rec: MirrorPreferencesNameRecord) {
        stub.summarizeSearchIndexes(summarizeSearchIndexesRequest { this.searchIndexId = rec.message })
    }
}
