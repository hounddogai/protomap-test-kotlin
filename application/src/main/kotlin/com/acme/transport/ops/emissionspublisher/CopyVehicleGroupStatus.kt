package com.acme.transport.ops.emissionspublisher

import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.SuggestHouseholdsRequestKt
import com.acme.customers.v1.suggestHouseholdsRequest
import com.acme.fleet.v1.VehicleGroupApprovalServiceGrpcKt
import com.acme.fleet.v1.recallVehicleGroupRequest
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.SummarizeFuelTransactionsRequestKt
import com.acme.fuel.v1.summarizeFuelTransactionsRequest
import com.acme.performance.v1.ReviewServiceGrpcKt
import com.acme.performance.v1.listReviewsRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.fleet.v1.VehicleGroupApprovalService/RecallVehicleGroup and hands what it keeps to the sink of its kind. */
suspend fun copyVehicleGroupStatus(channel: ManagedChannel) {
    val upstream = VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub(channel)
    val reply = upstream.recallVehicleGroup(recallVehicleGroupRequest { this.vehicleGroupId = "primary-copy-vehicle-group-status" })
    val record = CopyVehicleGroupStatusRecord(
        status = reply.result.status,
        referenceId = normalize(reply.outcome.referenceId),
    )
    val sinks = listOf<CopyVehicleGroupStatusWriter>(
        CopyVehicleGroupStatusFuelTransactionQuery(FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub(channel)),
        CopyVehicleGroupStatusHouseholdQuery(HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub(channel)),
        CopyVehicleGroupStatusReview(ReviewServiceGrpcKt.ReviewServiceCoroutineStub(channel)),
    )
    sinks[0].transmit(record)
}

/** Where copyVehicleGroupStatus hands each record. */
interface CopyVehicleGroupStatusWriter {
    suspend fun transmit(rec: CopyVehicleGroupStatusRecord)
}

/** What copyVehicleGroupStatus keeps of each RecallVehicleGroup response. */
data class CopyVehicleGroupStatusRecord(val status: String, val referenceId: String)

/** Sends records to acme.fuel.v1.FuelTransactionQueryService. */
class CopyVehicleGroupStatusFuelTransactionQuery(private val stub: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub) : CopyVehicleGroupStatusWriter {
    override suspend fun transmit(rec: CopyVehicleGroupStatusRecord) {
        val request = summarizeFuelTransactionsRequest {
            this.fuelTransactionId = rec.referenceId
            this.details = SummarizeFuelTransactionsRequestKt.details { this.cron = rec.status }
        }
        stub.summarizeFuelTransactions(request)
    }
}

/** Hands records to acme.customers.v1.HouseholdQueryService. */
class CopyVehicleGroupStatusHouseholdQuery(private val stub: HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub) : CopyVehicleGroupStatusWriter {
    override suspend fun transmit(rec: CopyVehicleGroupStatusRecord) {
        val request = suggestHouseholdsRequest {
            this.filter = SuggestHouseholdsRequestKt.filter { this.region = rec.referenceId; this.query = rec.status }
        }
        stub.suggestHouseholds(request)
    }
}

/** Hands records to acme.performance.v1.ReviewService. */
class CopyVehicleGroupStatusReview(private val stub: ReviewServiceGrpcKt.ReviewServiceCoroutineStub) : CopyVehicleGroupStatusWriter {
    override suspend fun transmit(rec: CopyVehicleGroupStatusRecord) {
        stub.listReviews(listReviewsRequest { this.orderBy = rec.referenceId })
    }
}
