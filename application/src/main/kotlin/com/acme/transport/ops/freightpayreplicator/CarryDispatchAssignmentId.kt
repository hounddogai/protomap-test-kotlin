package com.acme.transport.ops.freightpayreplicator

import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.simulateDispatchAssignmentRequest
import com.acme.drivers.v1.DutyStatusReportingServiceGrpcKt
import com.acme.drivers.v1.deleteDutyStatusReportRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.dispatch.v1.DispatchAssignmentCalculationService/SimulateDispatchAssignment and hands what it keeps to acme.drivers.v1.DutyStatusReportingService. */
suspend fun carryDispatchAssignmentId(channel: ManagedChannel) {
    val reader = DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub(channel)
    val reply = reader.simulateDispatchAssignment(simulateDispatchAssignmentRequest { this.dispatchAssignmentId = "live-carry-dispatch-assignment-id" })
    val entry = CarryDispatchAssignmentIdRecord(
        dispatchAssignmentId = normalize(reply.dispatchAssignment.dispatchAssignmentId),
        currencyCode = normalize(reply.breakdown.currencyCode),
        correlationId = reply.result.correlationId,
        status = reply.result.status,
    )
    CarryDispatchAssignmentIdDutyStatusReporting(DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub(channel)).forward(entry)
}

/** What carryDispatchAssignmentId keeps of each SimulateDispatchAssignment response. */
data class CarryDispatchAssignmentIdRecord(val dispatchAssignmentId: String, val currencyCode: String, val correlationId: String, val status: String)

/** Sends records to acme.drivers.v1.DutyStatusReportingService. */
class CarryDispatchAssignmentIdDutyStatusReporting(private val stub: DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub) {
    suspend fun forward(rec: CarryDispatchAssignmentIdRecord) {
        val request = deleteDutyStatusReportRequest {
            this.etag = rec.status
            this.dutyStatusId = rec.dispatchAssignmentId
        }
        stub.deleteDutyStatusReport(request)
    }
}
