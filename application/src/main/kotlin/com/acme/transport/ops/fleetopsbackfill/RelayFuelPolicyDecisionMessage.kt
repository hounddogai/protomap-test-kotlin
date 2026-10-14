package com.acme.transport.ops.fleetopsbackfill

import com.acme.driverapp.v1.CommentDriverInboxApprovalRequestKt
import com.acme.driverapp.v1.DriverInboxApprovalServiceGrpcKt
import com.acme.driverapp.v1.commentDriverInboxApprovalRequest
import com.acme.drivers.v1.DutyStatusCalculationServiceGrpcKt
import com.acme.drivers.v1.compareDutyStatusOptionsRequest
import com.acme.drivers.v1.dutyStatus
import com.acme.fleet.v1.VehicleServiceGrpcKt
import com.acme.fleet.v1.getVehicleRequest
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.explainFuelPolicyDecisionRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.validateYardRequest
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelPolicyValidationService/ExplainFuelPolicyDecision and hands what it keeps to the sink of its kind. */
suspend fun relayFuelPolicyDecisionMessage(channel: ManagedChannel) {
    val reader = FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { reader.explainFuelPolicyDecision(explainFuelPolicyDecisionRequest { this.reason = "primary-relay-fuel-policy-decision-message" }) }
    val row = RelayFuelPolicyDecisionMessageRecord(
        message = normalize(reply.result.message),
        fuelPolicyId = normalize(reply.fuelPolicy.fuelPolicyId),
        outcomeMessage = reply.outcome.message,
        status = reply.outcome.status,
    )
    val sinks = listOf<RelayFuelPolicyDecisionMessageSink>(
        RelayFuelPolicyDecisionMessageVehicle(VehicleServiceGrpcKt.VehicleServiceCoroutineStub(channel)),
        RelayFuelPolicyDecisionMessageDutyStatusCalculation(DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub(channel)),
        RelayFuelPolicyDecisionMessageDriverInboxApproval(DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub(channel)),
        RelayFuelPolicyDecisionMessageYardValidation(YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub(channel)),
    )
    sinks[2].deliver(row)
}

/** Where relayFuelPolicyDecisionMessage hands each record. */
interface RelayFuelPolicyDecisionMessageSink {
    suspend fun deliver(rec: RelayFuelPolicyDecisionMessageRecord)
}

/** What relayFuelPolicyDecisionMessage keeps of each ExplainFuelPolicyDecision response. */
data class RelayFuelPolicyDecisionMessageRecord(val message: String, val fuelPolicyId: String, val outcomeMessage: String, val status: String)

/** Sends records to acme.fleet.v1.VehicleService. */
class RelayFuelPolicyDecisionMessageVehicle(private val stub: VehicleServiceGrpcKt.VehicleServiceCoroutineStub) : RelayFuelPolicyDecisionMessageSink {
    override suspend fun deliver(rec: RelayFuelPolicyDecisionMessageRecord) {
        stub.getVehicle(getVehicleRequest { this.vehicleId = rec.message })
    }
}

/** Sends records to acme.drivers.v1.DutyStatusCalculationService. */
class RelayFuelPolicyDecisionMessageDutyStatusCalculation(private val stub: DutyStatusCalculationServiceGrpcKt.DutyStatusCalculationServiceCoroutineStub) : RelayFuelPolicyDecisionMessageSink {
    override suspend fun deliver(rec: RelayFuelPolicyDecisionMessageRecord) {
        val request = compareDutyStatusOptionsRequest {
            this.dutyStatus = dutyStatus { this.source = rec.message }
        }
        stub.compareDutyStatusOptions(request)
    }
}

/** Sends records to acme.driverapp.v1.DriverInboxApprovalService. */
class RelayFuelPolicyDecisionMessageDriverInboxApproval(private val stub: DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub) : RelayFuelPolicyDecisionMessageSink {
    override suspend fun deliver(rec: RelayFuelPolicyDecisionMessageRecord) {
        val request = commentDriverInboxApprovalRequest {
            this.details = CommentDriverInboxApprovalRequestKt.details { this.comment = rec.status; this.approverEmail = rec.outcomeMessage }
        }
        RpcBoundary.run("acme.driverapp.v1.DriverInboxApprovalService/CommentDriverInboxApproval") { stub.commentDriverInboxApproval(request) }
    }
}

/** Writes records to acme.yards.v1.YardValidationService. */
class RelayFuelPolicyDecisionMessageYardValidation(private val stub: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub) : RelayFuelPolicyDecisionMessageSink {
    override suspend fun deliver(rec: RelayFuelPolicyDecisionMessageRecord) {
        RpcBoundary.run("acme.yards.v1.YardValidationService/ValidateYard") { stub.validateYard(validateYardRequest { this.yardId = rec.message; this.reason = rec.fuelPolicyId }) }
    }
}
