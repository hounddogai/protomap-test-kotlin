package com.acme.transport.ops.crewingrelay

import com.acme.customers.v1.ConsentRecordApprovalServiceGrpcKt
import com.acme.customers.v1.RecallConsentRecordRequestKt
import com.acme.customers.v1.recallConsentRecordRequest
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.estimateVehicleGroupRequest
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.RecallFuelPolicyRequestKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.recallFuelPolicyRequest
import com.acme.performance.v1.ReviewQueryServiceGrpcKt
import com.acme.performance.v1.peekReviewRequest
import com.acme.recruiting.v1.JobOpeningServiceGrpcKt
import com.acme.recruiting.v1.createJobOpeningRequest
import com.acme.recruiting.v1.jobOpening
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.fleet.v1.VehicleGroupCalculationService/EstimateVehicleGroup and hands what it keeps to the sink of its kind. */
suspend fun mirrorVehicleGroupId(channel: ManagedChannel) {
    val upstream = VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub(channel)
    val found = upstream.estimateVehicleGroup(estimateVehicleGroupRequest { this.vehicleGroupId = "default-mirror-vehicle-group-id" })
    val row = MirrorVehicleGroupIdRecord(
        vehicleGroupId = found.vehicleGroup.vehicleGroupId,
        correlationId = normalize(found.result.correlationId),
    )
    val sinks = mapOf<String, MirrorVehicleGroupIdSink>(
        "consent-record-approval" to MirrorVehicleGroupIdConsentRecordApproval(ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub(channel)),
        "job-opening" to MirrorVehicleGroupIdJobOpening(JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub(channel)),
        "review-query" to MirrorVehicleGroupIdReviewQuery(ReviewQueryServiceGrpcKt.ReviewQueryServiceCoroutineStub(channel)),
        "fuel-policy-approval" to MirrorVehicleGroupIdFuelPolicyApproval(FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "consent-record-approval") continue
        sinks[kind]?.write(row)
    }
}

/** Where mirrorVehicleGroupId hands each record. */
interface MirrorVehicleGroupIdSink {
    suspend fun write(rec: MirrorVehicleGroupIdRecord)
}

/** What mirrorVehicleGroupId keeps of each EstimateVehicleGroup response. */
data class MirrorVehicleGroupIdRecord(val vehicleGroupId: String, val correlationId: String)

/** Writes records to acme.customers.v1.ConsentRecordApprovalService. */
class MirrorVehicleGroupIdConsentRecordApproval(private val stub: ConsentRecordApprovalServiceGrpcKt.ConsentRecordApprovalServiceCoroutineStub) : MirrorVehicleGroupIdSink {
    override suspend fun write(rec: MirrorVehicleGroupIdRecord) {
        val request = recallConsentRecordRequest {
            this.details = RecallConsentRecordRequestKt.details { this.comment = rec.correlationId }
        }
        RpcBoundary.run("acme.customers.v1.ConsentRecordApprovalService/RecallConsentRecord") { stub.recallConsentRecord(request) }
    }
}

/** Writes records to acme.recruiting.v1.JobOpeningService. */
class MirrorVehicleGroupIdJobOpening(private val stub: JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub) : MirrorVehicleGroupIdSink {
    override suspend fun write(rec: MirrorVehicleGroupIdRecord) {
        val request = createJobOpeningRequest {
            this.jobOpening = jobOpening { this.notes = rec.vehicleGroupId }
        }
        RpcBoundary.run("acme.recruiting.v1.JobOpeningService/CreateJobOpening") { stub.createJobOpening(request) }
    }
}

/** Writes records to acme.performance.v1.ReviewQueryService. */
class MirrorVehicleGroupIdReviewQuery(private val stub: ReviewQueryServiceGrpcKt.ReviewQueryServiceCoroutineStub) : MirrorVehicleGroupIdSink {
    override suspend fun write(rec: MirrorVehicleGroupIdRecord) {
        RpcBoundary.run("acme.performance.v1.ReviewQueryService/PeekReview") { stub.peekReview(peekReviewRequest { this.reviewId = rec.vehicleGroupId }) }
    }
}

/** Hands records to acme.fuel.v1.FuelPolicyApprovalService. */
class MirrorVehicleGroupIdFuelPolicyApproval(private val stub: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub) : MirrorVehicleGroupIdSink {
    override suspend fun write(rec: MirrorVehicleGroupIdRecord) {
        val request = recallFuelPolicyRequest {
            this.details = RecallFuelPolicyRequestKt.details { this.approverEmail = rec.correlationId }
            this.fuelPolicy = fuelPolicy { this.description = rec.vehicleGroupId }
        }
        stub.recallFuelPolicy(request)
    }
}
