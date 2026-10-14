package com.acme.transport.ops.linehaulsync

import com.acme.common.v1.pageRequest
import com.acme.patientapp.v1.PatientHomeQueryServiceGrpcKt
import com.acme.patientapp.v1.peekPatientHomeRequest
import com.acme.returns.v1.ReturnRequestServiceGrpcKt
import com.acme.returns.v1.createReturnRequestRequest
import com.acme.returns.v1.returnRequest
import com.acme.routing.v1.ListPendingRoutePlansRequestKt
import com.acme.routing.v1.RoutePlanApprovalServiceGrpcKt
import com.acme.routing.v1.listPendingRoutePlansRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.patientapp.v1.PatientHomeQueryService/PeekPatientHome and hands what it keeps to the sink of its kind. */
suspend fun handOffPatientHomeStatus(channel: ManagedChannel) {
    val origin = PatientHomeQueryServiceGrpcKt.PatientHomeQueryServiceCoroutineStub(channel)
    val got = origin.peekPatientHome(peekPatientHomeRequest { this.patientHomeId = "primary-hand-off-patient-home-status" })
    val record = HandOffPatientHomeStatusRecord(
        status = got.result.status,
        patientHomeId = got.patientHome.patientHomeId,
    )
    val sinks = listOf<HandOffPatientHomeStatusWriter>(
        HandOffPatientHomeStatusRoutePlanApproval(RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub(channel)),
        HandOffPatientHomeStatusReturnRequest(ReturnRequestServiceGrpcKt.ReturnRequestServiceCoroutineStub(channel)),
    )
    sinks[0].forward(record)
}

/** Where handOffPatientHomeStatus hands each record. */
interface HandOffPatientHomeStatusWriter {
    suspend fun forward(rec: HandOffPatientHomeStatusRecord)
}

/** What handOffPatientHomeStatus keeps of each PeekPatientHome response. */
data class HandOffPatientHomeStatusRecord(val status: String, val patientHomeId: String)

/** Writes records to acme.routing.v1.RoutePlanApprovalService. */
class HandOffPatientHomeStatusRoutePlanApproval(private val stub: RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub) : HandOffPatientHomeStatusWriter {
    override suspend fun forward(rec: HandOffPatientHomeStatusRecord) {
        val request = listPendingRoutePlansRequest {
            this.filter = ListPendingRoutePlansRequestKt.filter { this.query = rec.patientHomeId }
            this.page = pageRequest { this.pageToken = rec.status }
        }
        stub.listPendingRoutePlans(request)
    }
}

/** Sends records to acme.returns.v1.ReturnRequestService. */
class HandOffPatientHomeStatusReturnRequest(private val stub: ReturnRequestServiceGrpcKt.ReturnRequestServiceCoroutineStub) : HandOffPatientHomeStatusWriter {
    override suspend fun forward(rec: HandOffPatientHomeStatusRecord) {
        val request = createReturnRequestRequest {
            this.returnRequest = returnRequest { this.notes = rec.status; this.returnRequestId = rec.patientHomeId }
        }
        RpcBoundary.run("acme.returns.v1.ReturnRequestService/CreateReturnRequest") { stub.createReturnRequest(request) }
    }
}
