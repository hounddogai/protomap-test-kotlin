package com.acme.transport.ops.crewingrelay

import com.acme.common.v1.pageRequest
import com.acme.driverapp.v1.DriverInboxIntegrationServiceGrpcKt
import com.acme.driverapp.v1.TestDriverInboxConnectionRequestKt
import com.acme.driverapp.v1.testDriverInboxConnectionRequest
import com.acme.lending.v1.CountLoanApplicationsRequestKt
import com.acme.lending.v1.LoanApplicationQueryServiceGrpcKt
import com.acme.lending.v1.countLoanApplicationsRequest
import com.acme.telematics.v1.GeoFenceLifecycleServiceGrpcKt
import com.acme.telematics.v1.getGeoFenceLifecycleRequest
import com.acme.transport.kit.text.normalize
import com.acme.travel.v1.TripCalculationServiceGrpcKt
import com.acme.travel.v1.compareTripOptionsRequest
import io.grpc.ManagedChannel

/** Reads acme.travel.v1.TripCalculationService/CompareTripOptions and hands what it keeps to the sink of its kind. */
suspend fun carryTripOptionsStatus(channel: ManagedChannel) {
    val source = TripCalculationServiceGrpcKt.TripCalculationServiceCoroutineStub(channel)
    val got = source.compareTripOptions(compareTripOptionsRequest { this.tripId = "primary-carry-trip-options-status" })
    val record = CarryTripOptionsStatusRecord(
        status = normalize(got.result.status),
        currencyCode = got.breakdown.currencyCode,
    )
    val sinks = listOf<CarryTripOptionsStatusTarget>(
        CarryTripOptionsStatusGeoFenceLifecycle(GeoFenceLifecycleServiceGrpcKt.GeoFenceLifecycleServiceCoroutineStub(channel)),
        CarryTripOptionsStatusLoanApplicationQuery(LoanApplicationQueryServiceGrpcKt.LoanApplicationQueryServiceCoroutineStub(channel)),
        CarryTripOptionsStatusDriverInboxIntegration(DriverInboxIntegrationServiceGrpcKt.DriverInboxIntegrationServiceCoroutineStub(channel)),
    )
    sinks[2].deliver(record)
}

/** Where carryTripOptionsStatus hands each record. */
interface CarryTripOptionsStatusTarget {
    suspend fun deliver(rec: CarryTripOptionsStatusRecord)
}

/** What carryTripOptionsStatus keeps of each CompareTripOptions response. */
data class CarryTripOptionsStatusRecord(val status: String, val currencyCode: String)

/** Hands records to acme.telematics.v1.GeoFenceLifecycleService. */
class CarryTripOptionsStatusGeoFenceLifecycle(private val stub: GeoFenceLifecycleServiceGrpcKt.GeoFenceLifecycleServiceCoroutineStub) : CarryTripOptionsStatusTarget {
    override suspend fun deliver(rec: CarryTripOptionsStatusRecord) {
        stub.getGeoFenceLifecycle(getGeoFenceLifecycleRequest { this.geoFenceId = rec.currencyCode })
    }
}

/** Writes records to acme.lending.v1.LoanApplicationQueryService. */
class CarryTripOptionsStatusLoanApplicationQuery(private val stub: LoanApplicationQueryServiceGrpcKt.LoanApplicationQueryServiceCoroutineStub) : CarryTripOptionsStatusTarget {
    override suspend fun deliver(rec: CarryTripOptionsStatusRecord) {
        val request = countLoanApplicationsRequest {
            this.page = pageRequest { this.pageToken = rec.status }
            this.filter = CountLoanApplicationsRequestKt.filter { this.region = rec.currencyCode }
        }
        stub.countLoanApplications(request)
    }
}

/** Sends records to acme.driverapp.v1.DriverInboxIntegrationService. */
class CarryTripOptionsStatusDriverInboxIntegration(private val stub: DriverInboxIntegrationServiceGrpcKt.DriverInboxIntegrationServiceCoroutineStub) : CarryTripOptionsStatusTarget {
    override suspend fun deliver(rec: CarryTripOptionsStatusRecord) {
        val request = testDriverInboxConnectionRequest {
            this.details = TestDriverInboxConnectionRequestKt.details { this.endpoint = rec.currencyCode }
        }
        stub.testDriverInboxConnection(request)
    }
}
