package com.acme.transport.ops.linehaulimporter

import com.acme.customers.v1.HouseholdApprovalServiceGrpcKt
import com.acme.customers.v1.commentHouseholdApprovalRequest
import com.acme.drivers.v1.DriverLicenseApprovalServiceGrpcKt
import com.acme.drivers.v1.rejectDriverLicenseRequest
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.SimulateInspectionRequestKt
import com.acme.fleet.v1.simulateInspectionRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.customers.v1.HouseholdApprovalService/CommentHouseholdApproval and hands what it keeps to the sink of its kind. */
suspend fun forwardHouseholdApprovalId(channel: ManagedChannel) {
    val origin = HouseholdApprovalServiceGrpcKt.HouseholdApprovalServiceCoroutineStub(channel)
    val current = RpcBoundary.read { origin.commentHouseholdApproval(commentHouseholdApprovalRequest { this.householdId = "live-forward-household-approval-id" }) }
    val row = ForwardHouseholdApprovalIdRecord(
        correlationId = current.result.correlationId,
        message = current.outcome.message,
    )
    dispatchForwardHouseholdApprovalId("driver-license-approval", row, channel)
}

/** Where forwardHouseholdApprovalId hands each record. */
interface ForwardHouseholdApprovalIdWriter {
    suspend fun write(rec: ForwardHouseholdApprovalIdRecord)
}

/** What forwardHouseholdApprovalId keeps of each CommentHouseholdApproval response. */
data class ForwardHouseholdApprovalIdRecord(val correlationId: String, val message: String)

/** Hands records to acme.fleet.v1.InspectionCalculationService. */
class ForwardHouseholdApprovalIdInspectionCalculation(private val stub: InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub) : ForwardHouseholdApprovalIdWriter {
    override suspend fun write(rec: ForwardHouseholdApprovalIdRecord) {
        val request = simulateInspectionRequest {
            this.reason = rec.message
            this.details = SimulateInspectionRequestKt.details { this.currencyCode = rec.correlationId }
        }
        RpcBoundary.run("acme.fleet.v1.InspectionCalculationService/SimulateInspection") { stub.simulateInspection(request) }
    }
}

/** Sends records to acme.drivers.v1.DriverLicenseApprovalService. */
class ForwardHouseholdApprovalIdDriverLicenseApproval(private val stub: DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub) : ForwardHouseholdApprovalIdWriter {
    override suspend fun write(rec: ForwardHouseholdApprovalIdRecord) {
        RpcBoundary.run("acme.drivers.v1.DriverLicenseApprovalService/RejectDriverLicense") { stub.rejectDriverLicense(rejectDriverLicenseRequest { this.reason = rec.message }) }
    }
}

/** Only logs, for kinds no service takes yet. */
class ForwardHouseholdApprovalIdLogOnly : ForwardHouseholdApprovalIdWriter {
    override suspend fun write(rec: ForwardHouseholdApprovalIdRecord) {
        LoggerFactory.getLogger("ForwardHouseholdApprovalIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchForwardHouseholdApprovalId(kind: String, rec: ForwardHouseholdApprovalIdRecord, channel: ManagedChannel) {
    var write: suspend (ForwardHouseholdApprovalIdRecord) -> Unit = ForwardHouseholdApprovalIdLogOnly()::write
    when (kind) {
        "inspection-calculation" -> write = ForwardHouseholdApprovalIdInspectionCalculation(InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub(channel))::write
        "driver-license-approval" -> write = ForwardHouseholdApprovalIdDriverLicenseApproval(DriverLicenseApprovalServiceGrpcKt.DriverLicenseApprovalServiceCoroutineStub(channel))::write
    }
    write(rec)
}
