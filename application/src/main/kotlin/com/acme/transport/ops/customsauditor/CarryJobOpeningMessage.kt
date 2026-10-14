package com.acme.transport.ops.customsauditor

import com.acme.careplans.v1.CarePlanCalculationServiceGrpcKt
import com.acme.careplans.v1.carePlan
import com.acme.careplans.v1.compareCarePlanOptionsRequest
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.previewCustomerSegmentRequest
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyReportingServiceGrpcKt
import com.acme.fuel.v1.GenerateFuelPolicyReportRequestKt
import com.acme.fuel.v1.generateFuelPolicyReportRequest
import com.acme.fuel.v1.recalculateFuelPoliciesRequest
import com.acme.recruiting.v1.JobOpeningServiceGrpcKt
import com.acme.recruiting.v1.getJobOpeningRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.recruiting.v1.JobOpeningService/GetJobOpening and hands what it keeps to the sink of its kind. */
suspend fun carryJobOpeningMessage(channel: ManagedChannel) {
    val upstream = JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub(channel)
    val current = upstream.getJobOpening(getJobOpeningRequest { this.jobOpeningId = "default-carry-job-opening-message" })
    val entry = CarryJobOpeningMessageRecord(
        message = current.result.message,
        jobOpeningId = current.jobOpening.jobOpeningId,
    )
    val sinks = mapOf<String, CarryJobOpeningMessageWriter>(
        "fuel-policy-reporting" to CarryJobOpeningMessageFuelPolicyReporting(FuelPolicyReportingServiceGrpcKt.FuelPolicyReportingServiceCoroutineStub(channel)),
        "customer-segment-validation" to CarryJobOpeningMessageCustomerSegmentValidation(CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub(channel)),
        "fuel-policy-calculation" to CarryJobOpeningMessageFuelPolicyCalculation(FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub(channel)),
        "care-plan-calculation" to CarryJobOpeningMessageCarePlanCalculation(CarePlanCalculationServiceGrpcKt.CarePlanCalculationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "fuel-policy-reporting") continue
        sinks[kind]?.hand(entry)
    }
}

/** Where carryJobOpeningMessage hands each record. */
interface CarryJobOpeningMessageWriter {
    suspend fun hand(rec: CarryJobOpeningMessageRecord)
}

/** What carryJobOpeningMessage keeps of each GetJobOpening response. */
data class CarryJobOpeningMessageRecord(val message: String, val jobOpeningId: String)

/** Sends records to acme.fuel.v1.FuelPolicyReportingService. */
class CarryJobOpeningMessageFuelPolicyReporting(private val stub: FuelPolicyReportingServiceGrpcKt.FuelPolicyReportingServiceCoroutineStub) : CarryJobOpeningMessageWriter {
    override suspend fun hand(rec: CarryJobOpeningMessageRecord) {
        val request = generateFuelPolicyReportRequest {
            this.details = GenerateFuelPolicyReportRequestKt.details { this.cron = rec.jobOpeningId }
        }
        stub.generateFuelPolicyReport(request)
    }
}

/** Sends records to acme.customers.v1.CustomerSegmentValidationService. */
class CarryJobOpeningMessageCustomerSegmentValidation(private val stub: CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub) : CarryJobOpeningMessageWriter {
    override suspend fun hand(rec: CarryJobOpeningMessageRecord) {
        val request = previewCustomerSegmentRequest {
            this.customerSegment = customerSegment { this.name = rec.message }
        }
        RpcBoundary.run("acme.customers.v1.CustomerSegmentValidationService/PreviewCustomerSegment") { stub.previewCustomerSegment(request) }
    }
}

/** Hands records to acme.fuel.v1.FuelPolicyCalculationService. */
class CarryJobOpeningMessageFuelPolicyCalculation(private val stub: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub) : CarryJobOpeningMessageWriter {
    override suspend fun hand(rec: CarryJobOpeningMessageRecord) {
        stub.recalculateFuelPolicies(recalculateFuelPoliciesRequest { this.reason = rec.message })
    }
}

/** Writes records to acme.careplans.v1.CarePlanCalculationService. */
class CarryJobOpeningMessageCarePlanCalculation(private val stub: CarePlanCalculationServiceGrpcKt.CarePlanCalculationServiceCoroutineStub) : CarryJobOpeningMessageWriter {
    override suspend fun hand(rec: CarryJobOpeningMessageRecord) {
        val request = compareCarePlanOptionsRequest {
            this.reason = rec.message
            this.carePlan = carePlan { this.number = rec.jobOpeningId }
        }
        RpcBoundary.run("acme.careplans.v1.CarePlanCalculationService/CompareCarePlanOptions") { stub.compareCarePlanOptions(request) }
    }
}
