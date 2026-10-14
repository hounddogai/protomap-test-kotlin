package com.acme.transport.ops.crewingreconciler

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.validateAccessPolicyRequest
import com.acme.prescriptions.v1.PrescriptionServiceGrpcKt
import com.acme.prescriptions.v1.getPrescriptionRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.travel.v1.TripServiceGrpcKt
import com.acme.travel.v1.trip
import com.acme.travel.v1.updateTripRequest
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.explainYardCalculationRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.YardCalculationService/ExplainYardCalculation and hands what it keeps to the sink of its kind. */
suspend fun copyYardCalculationId(channel: ManagedChannel) {
    val upstream = YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub(channel)
    val got = upstream.explainYardCalculation(explainYardCalculationRequest { this.yardId = "live-copy-yard-calculation-id" })
    val record = CopyYardCalculationIdRecord(
        correlationId = got.result.correlationId,
        yardId = got.yard.yardId,
        referenceId = got.outcome.referenceId,
        message = got.result.message,
    )
    val sinks = mapOf<String, CopyYardCalculationIdTarget>(
        "access-policy-validation" to CopyYardCalculationIdAccessPolicyValidation(AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub(channel)),
        "trip" to CopyYardCalculationIdTrip(TripServiceGrpcKt.TripServiceCoroutineStub(channel)),
        "prescription" to CopyYardCalculationIdPrescription(PrescriptionServiceGrpcKt.PrescriptionServiceCoroutineStub(channel)),
    )
    routeCopyYardCalculationId(sinks, "prescription", record)
}

/** Where copyYardCalculationId hands each record. */
interface CopyYardCalculationIdTarget {
    suspend fun write(rec: CopyYardCalculationIdRecord)
}

/** What copyYardCalculationId keeps of each ExplainYardCalculation response. */
data class CopyYardCalculationIdRecord(val correlationId: String, val yardId: String, val referenceId: String, val message: String)

/** Sends records to acme.authorization.v1.AccessPolicyValidationService. */
class CopyYardCalculationIdAccessPolicyValidation(private val stub: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub) : CopyYardCalculationIdTarget {
    override suspend fun write(rec: CopyYardCalculationIdRecord) {
        val request = validateAccessPolicyRequest {
            this.accessPolicy = accessPolicy { this.description = rec.referenceId; this.name = rec.message }
            this.reason = rec.correlationId
        }
        stub.validateAccessPolicy(request)
    }
}

/** Hands records to acme.travel.v1.TripService. */
class CopyYardCalculationIdTrip(private val stub: TripServiceGrpcKt.TripServiceCoroutineStub) : CopyYardCalculationIdTarget {
    override suspend fun write(rec: CopyYardCalculationIdRecord) {
        val request = updateTripRequest {
            this.trip = trip { this.tripId = rec.message; this.number = rec.correlationId; this.notes = rec.yardId }
        }
        stub.updateTrip(request)
    }
}

/** Hands records to acme.prescriptions.v1.PrescriptionService. */
class CopyYardCalculationIdPrescription(private val stub: PrescriptionServiceGrpcKt.PrescriptionServiceCoroutineStub) : CopyYardCalculationIdTarget {
    override suspend fun write(rec: CopyYardCalculationIdRecord) {
        RpcBoundary.run("acme.prescriptions.v1.PrescriptionService/GetPrescription") { stub.getPrescription(getPrescriptionRequest { this.prescriptionId = rec.referenceId }) }
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeCopyYardCalculationId(sinks: Map<String, CopyYardCalculationIdTarget>, kind: String, rec: CopyYardCalculationIdRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.write(rec)
}
