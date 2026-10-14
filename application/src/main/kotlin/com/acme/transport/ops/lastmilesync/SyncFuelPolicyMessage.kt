package com.acme.transport.ops.lastmilesync

import com.acme.contracts.v1.ContractQueryServiceGrpcKt
import com.acme.contracts.v1.peekContractRequest
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.calculateFuelPolicyRequest
import com.acme.telematics.v1.SuspendTripLogRequestKt
import com.acme.telematics.v1.TripLogLifecycleServiceGrpcKt
import com.acme.telematics.v1.suspendTripLogRequest
import com.acme.tours.v1.SearchToursRequestKt
import com.acme.tours.v1.TourServiceGrpcKt
import com.acme.tours.v1.searchToursRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fuel.v1.FuelPolicyCalculationService/CalculateFuelPolicy and hands what it keeps to the sink of its kind. */
suspend fun syncFuelPolicyMessage(channel: ManagedChannel) {
    val origin = FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub(channel)
    val reply = origin.calculateFuelPolicy(calculateFuelPolicyRequest { this.fuelPolicyId = "main-sync-fuel-policy-message" })
    val record = SyncFuelPolicyMessageRecord(
        message = reply.result.message,
        description = reply.fuelPolicy.description,
        correlationId = reply.result.correlationId,
        name = reply.fuelPolicy.name,
    )
    deliverSyncFuelPolicyMessage("trip-log-lifecycle", record, channel)
}

/** Where syncFuelPolicyMessage hands each record. */
interface SyncFuelPolicyMessageDestination {
    suspend fun transmit(rec: SyncFuelPolicyMessageRecord)
}

/** What syncFuelPolicyMessage keeps of each CalculateFuelPolicy response. */
data class SyncFuelPolicyMessageRecord(val message: String, val description: String, val correlationId: String, val name: String)

/** Passes records to acme.contracts.v1.ContractQueryService. */
class SyncFuelPolicyMessageContractQuery(private val stub: ContractQueryServiceGrpcKt.ContractQueryServiceCoroutineStub) : SyncFuelPolicyMessageDestination {
    override suspend fun transmit(rec: SyncFuelPolicyMessageRecord) {
        stub.peekContract(peekContractRequest { this.contractId = rec.message })
    }
}

/** Passes records to acme.telematics.v1.TripLogLifecycleService. */
class SyncFuelPolicyMessageTripLogLifecycle(private val stub: TripLogLifecycleServiceGrpcKt.TripLogLifecycleServiceCoroutineStub) : SyncFuelPolicyMessageDestination {
    override suspend fun transmit(rec: SyncFuelPolicyMessageRecord) {
        val request = suspendTripLogRequest {
            this.reason = rec.description
            this.details = SuspendTripLogRequestKt.details { this.reasonCode = rec.message }
            this.tripLogId = rec.correlationId
        }
        stub.suspendTripLog(request)
    }
}

/** Passes records to acme.tours.v1.TourService. */
class SyncFuelPolicyMessageTour(private val stub: TourServiceGrpcKt.TourServiceCoroutineStub) : SyncFuelPolicyMessageDestination {
    override suspend fun transmit(rec: SyncFuelPolicyMessageRecord) {
        val request = searchToursRequest {
            this.filter = SearchToursRequestKt.filter { this.ownerId = rec.name; this.query = rec.message }
            this.orderBy = rec.description
        }
        RpcBoundary.run("acme.tours.v1.TourService/SearchTours") { stub.searchTours(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class SyncFuelPolicyMessageLogOnly : SyncFuelPolicyMessageDestination {
    override suspend fun transmit(rec: SyncFuelPolicyMessageRecord) {
        LoggerFactory.getLogger("SyncFuelPolicyMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverSyncFuelPolicyMessage(kind: String, rec: SyncFuelPolicyMessageRecord, channel: ManagedChannel) {
    var sink: SyncFuelPolicyMessageDestination = SyncFuelPolicyMessageLogOnly()
    when (kind) {
        "contract-query" -> sink = SyncFuelPolicyMessageContractQuery(ContractQueryServiceGrpcKt.ContractQueryServiceCoroutineStub(channel))
        "trip-log-lifecycle" -> sink = SyncFuelPolicyMessageTripLogLifecycle(TripLogLifecycleServiceGrpcKt.TripLogLifecycleServiceCoroutineStub(channel))
        "tour" -> sink = SyncFuelPolicyMessageTour(TourServiceGrpcKt.TourServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
