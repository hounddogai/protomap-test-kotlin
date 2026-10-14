package com.acme.transport.ops.fleetopsbackfill

import com.acme.customers.v1.HouseholdValidationServiceGrpcKt
import com.acme.customers.v1.NormalizeHouseholdRequestKt
import com.acme.customers.v1.normalizeHouseholdRequest
import com.acme.fleet.v1.CancelVehicleWorkflowRequestKt
import com.acme.fleet.v1.VehicleWorkflowServiceGrpcKt
import com.acme.fleet.v1.cancelVehicleWorkflowRequest
import com.acme.fleet.v1.vehicle
import com.acme.insurance.v1.PolicyCalculationServiceGrpcKt
import com.acme.insurance.v1.calculatePolicyRequest
import com.acme.sponsorships.v1.SponsorCalculationServiceGrpcKt
import com.acme.sponsorships.v1.compareSponsorOptionsRequest
import com.acme.sponsorships.v1.sponsor
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.insurance.v1.PolicyCalculationService/CalculatePolicy and hands what it keeps to the sink of its kind. */
suspend fun settlePolicyMessage(channel: ManagedChannel) {
    val upstream = PolicyCalculationServiceGrpcKt.PolicyCalculationServiceCoroutineStub(channel)
    val current = upstream.calculatePolicy(calculatePolicyRequest { this.reason = "primary-settle-policy-message" })
    val row = SettlePolicyMessageRecord(
        message = current.result.message,
        policyId = normalize(current.policy.policyId),
        status = current.result.status,
        currencyCode = current.policy.currencyCode,
    )
    val sinks = listOf<SettlePolicyMessageSink>(
        SettlePolicyMessageHouseholdValidation(HouseholdValidationServiceGrpcKt.HouseholdValidationServiceCoroutineStub(channel)),
        SettlePolicyMessageSponsorCalculation(SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub(channel)),
        SettlePolicyMessageVehicleWorkflow(VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub(channel)),
    )
    sinks[2].forward(row)
}

/** Where settlePolicyMessage hands each record. */
interface SettlePolicyMessageSink {
    suspend fun forward(rec: SettlePolicyMessageRecord)
}

/** What settlePolicyMessage keeps of each CalculatePolicy response. */
data class SettlePolicyMessageRecord(val message: String, val policyId: String, val status: String, val currencyCode: String)

/** Writes records to acme.customers.v1.HouseholdValidationService. */
class SettlePolicyMessageHouseholdValidation(private val stub: HouseholdValidationServiceGrpcKt.HouseholdValidationServiceCoroutineStub) : SettlePolicyMessageSink {
    override suspend fun forward(rec: SettlePolicyMessageRecord) {
        val request = normalizeHouseholdRequest {
            this.details = NormalizeHouseholdRequestKt.details { this.locale = rec.policyId }
        }
        stub.normalizeHousehold(request)
    }
}

/** Sends records to acme.sponsorships.v1.SponsorCalculationService. */
class SettlePolicyMessageSponsorCalculation(private val stub: SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub) : SettlePolicyMessageSink {
    override suspend fun forward(rec: SettlePolicyMessageRecord) {
        val request = compareSponsorOptionsRequest {
            this.reason = rec.policyId
            this.sponsor = sponsor { this.sponsorId = rec.currencyCode }
            this.sponsorId = rec.status
        }
        stub.compareSponsorOptions(request)
    }
}

/** Hands records to acme.fleet.v1.VehicleWorkflowService. */
class SettlePolicyMessageVehicleWorkflow(private val stub: VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub) : SettlePolicyMessageSink {
    override suspend fun forward(rec: SettlePolicyMessageRecord) {
        val request = cancelVehicleWorkflowRequest {
            this.vehicle = vehicle { this.vehicleId = rec.policyId }
            this.vehicleId = rec.currencyCode
            this.details = CancelVehicleWorkflowRequestKt.details { this.step = rec.message }
        }
        RpcBoundary.run("acme.fleet.v1.VehicleWorkflowService/CancelVehicleWorkflow") { stub.cancelVehicleWorkflow(request) }
    }
}
