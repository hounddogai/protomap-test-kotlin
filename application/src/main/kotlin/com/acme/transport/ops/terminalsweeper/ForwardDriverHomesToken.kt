package com.acme.transport.ops.terminalsweeper

import com.acme.driverapp.v1.DriverHomeServiceGrpcKt
import com.acme.driverapp.v1.searchDriverHomesRequest
import com.acme.drivers.v1.DriverLicenseLifecycleServiceGrpcKt
import com.acme.drivers.v1.getDriverLicenseLifecycleRequest
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.retryFuelTransactionImportRequest
import com.acme.routing.v1.RoutePlanValidationServiceGrpcKt
import com.acme.routing.v1.checkRoutePlanEligibilityRequest
import com.acme.routing.v1.routePlan
import com.acme.telematics.v1.TripLogSyncServiceGrpcKt
import com.acme.telematics.v1.syncTripLogsRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.driverapp.v1.DriverHomeService/SearchDriverHomes and hands what it keeps to the sink of its kind. */
suspend fun forwardDriverHomesToken(channel: ManagedChannel) {
    val source = DriverHomeServiceGrpcKt.DriverHomeServiceCoroutineStub(channel)
    val got = RpcBoundary.read { source.searchDriverHomes(searchDriverHomesRequest { this.orderBy = "default-forward-driver-homes-token" }) }
    val rec = ForwardDriverHomesTokenRecord(
        nextPageToken = normalize(got.pageInfo.nextPageToken),
        message = normalize(got.result.message),
        correlationId = got.result.correlationId,
        status = got.result.status,
    )
    val sinks = mapOf<String, ForwardDriverHomesTokenDestination>(
        "route-plan-validation" to ForwardDriverHomesTokenRoutePlanValidation(RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub(channel)),
        "fuel-transaction-import" to ForwardDriverHomesTokenFuelTransactionImport(FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub(channel)),
        "driver-license-lifecycle" to ForwardDriverHomesTokenDriverLicenseLifecycle(DriverLicenseLifecycleServiceGrpcKt.DriverLicenseLifecycleServiceCoroutineStub(channel)),
        "trip-log-sync" to ForwardDriverHomesTokenTripLogSync(TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "route-plan-validation") continue
        sinks[kind]?.deliver(rec)
    }
}

/** Where forwardDriverHomesToken hands each record. */
interface ForwardDriverHomesTokenDestination {
    suspend fun deliver(rec: ForwardDriverHomesTokenRecord)
}

/** What forwardDriverHomesToken keeps of each SearchDriverHomes response. */
data class ForwardDriverHomesTokenRecord(val nextPageToken: String, val message: String, val correlationId: String, val status: String)

/** Sends records to acme.routing.v1.RoutePlanValidationService. */
class ForwardDriverHomesTokenRoutePlanValidation(private val stub: RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub) : ForwardDriverHomesTokenDestination {
    override suspend fun deliver(rec: ForwardDriverHomesTokenRecord) {
        val request = checkRoutePlanEligibilityRequest {
            this.routePlan = routePlan { this.notes = rec.nextPageToken; this.number = rec.status }
            this.reason = rec.message
        }
        stub.checkRoutePlanEligibility(request)
    }
}

/** Passes records to acme.fuel.v1.FuelTransactionImportService. */
class ForwardDriverHomesTokenFuelTransactionImport(private val stub: FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub) : ForwardDriverHomesTokenDestination {
    override suspend fun deliver(rec: ForwardDriverHomesTokenRecord) {
        stub.retryFuelTransactionImport(retryFuelTransactionImportRequest { this.reason = rec.status })
    }
}

/** Hands records to acme.drivers.v1.DriverLicenseLifecycleService. */
class ForwardDriverHomesTokenDriverLicenseLifecycle(private val stub: DriverLicenseLifecycleServiceGrpcKt.DriverLicenseLifecycleServiceCoroutineStub) : ForwardDriverHomesTokenDestination {
    override suspend fun deliver(rec: ForwardDriverHomesTokenRecord) {
        stub.getDriverLicenseLifecycle(getDriverLicenseLifecycleRequest { this.driverLicenseId = rec.correlationId })
    }
}

/** Writes records to acme.telematics.v1.TripLogSyncService. */
class ForwardDriverHomesTokenTripLogSync(private val stub: TripLogSyncServiceGrpcKt.TripLogSyncServiceCoroutineStub) : ForwardDriverHomesTokenDestination {
    override suspend fun deliver(rec: ForwardDriverHomesTokenRecord) {
        val request = syncTripLogsRequest {
            this.tripLog = tripLog { this.number = rec.status; this.tripLogId = rec.message }
        }
        RpcBoundary.run("acme.telematics.v1.TripLogSyncService/SyncTripLogs") { stub.syncTripLogs(request) }
    }
}
