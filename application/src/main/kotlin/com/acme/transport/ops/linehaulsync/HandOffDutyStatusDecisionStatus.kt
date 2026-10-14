package com.acme.transport.ops.linehaulsync

import com.acme.customers.v1.HouseholdApprovalServiceGrpcKt
import com.acme.customers.v1.getHouseholdApprovalStatusRequest
import com.acme.drivers.v1.DutyStatusValidationServiceGrpcKt
import com.acme.drivers.v1.explainDutyStatusDecisionRequest
import com.acme.fleet.v1.ApproveInspectionRequestKt
import com.acme.fleet.v1.InspectionApprovalServiceGrpcKt
import com.acme.fleet.v1.approveInspectionRequest
import com.acme.fleet.v1.inspection
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.createUserRequest
import com.acme.identity.v1.user
import com.acme.telematics.v1.CommentTripLogApprovalRequestKt
import com.acme.telematics.v1.TripLogApprovalServiceGrpcKt
import com.acme.telematics.v1.commentTripLogApprovalRequest
import io.grpc.ManagedChannel

/** Reads acme.drivers.v1.DutyStatusValidationService/ExplainDutyStatusDecision and hands what it keeps to the sink of its kind. */
suspend fun handOffDutyStatusDecisionStatus(channel: ManagedChannel) {
    val reader = DutyStatusValidationServiceGrpcKt.DutyStatusValidationServiceCoroutineStub(channel)
    val got = reader.explainDutyStatusDecision(explainDutyStatusDecisionRequest { this.dutyStatusId = "nightly-hand-off-duty-status-decision-status" })
    val row = HandOffDutyStatusDecisionStatusRecord(
        status = got.result.status,
        kind = got.dutyStatus.kind,
    )
    val sinks = mapOf<String, HandOffDutyStatusDecisionStatusDestination>(
        "inspection-approval" to HandOffDutyStatusDecisionStatusInspectionApproval(InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub(channel)),
        "household-approval" to HandOffDutyStatusDecisionStatusHouseholdApproval(HouseholdApprovalServiceGrpcKt.HouseholdApprovalServiceCoroutineStub(channel)),
        "user" to HandOffDutyStatusDecisionStatusUser(UserServiceGrpcKt.UserServiceCoroutineStub(channel)),
        "trip-log-approval" to HandOffDutyStatusDecisionStatusTripLogApproval(TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "inspection-approval") continue
        sinks[kind]?.transmit(row)
    }
}

/** Where handOffDutyStatusDecisionStatus hands each record. */
interface HandOffDutyStatusDecisionStatusDestination {
    suspend fun transmit(rec: HandOffDutyStatusDecisionStatusRecord)
}

/** What handOffDutyStatusDecisionStatus keeps of each ExplainDutyStatusDecision response. */
data class HandOffDutyStatusDecisionStatusRecord(val status: String, val kind: String)

/** Passes records to acme.fleet.v1.InspectionApprovalService. */
class HandOffDutyStatusDecisionStatusInspectionApproval(private val stub: InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub) : HandOffDutyStatusDecisionStatusDestination {
    override suspend fun transmit(rec: HandOffDutyStatusDecisionStatusRecord) {
        val request = approveInspectionRequest {
            this.details = ApproveInspectionRequestKt.details { this.comment = rec.status }
            this.inspection = inspection { this.subject = rec.kind }
        }
        stub.approveInspection(request)
    }
}

/** Passes records to acme.customers.v1.HouseholdApprovalService. */
class HandOffDutyStatusDecisionStatusHouseholdApproval(private val stub: HouseholdApprovalServiceGrpcKt.HouseholdApprovalServiceCoroutineStub) : HandOffDutyStatusDecisionStatusDestination {
    override suspend fun transmit(rec: HandOffDutyStatusDecisionStatusRecord) {
        stub.getHouseholdApprovalStatus(getHouseholdApprovalStatusRequest { this.householdId = rec.status })
    }
}

/** Passes records to acme.identity.v1.UserService. */
class HandOffDutyStatusDecisionStatusUser(private val stub: UserServiceGrpcKt.UserServiceCoroutineStub) : HandOffDutyStatusDecisionStatusDestination {
    override suspend fun transmit(rec: HandOffDutyStatusDecisionStatusRecord) {
        val request = createUserRequest {
            this.requestId = rec.kind
            this.user = user { this.userId = rec.status }
        }
        stub.createUser(request)
    }
}

/** Writes records to acme.telematics.v1.TripLogApprovalService. */
class HandOffDutyStatusDecisionStatusTripLogApproval(private val stub: TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineStub) : HandOffDutyStatusDecisionStatusDestination {
    override suspend fun transmit(rec: HandOffDutyStatusDecisionStatusRecord) {
        val request = commentTripLogApprovalRequest {
            this.details = CommentTripLogApprovalRequestKt.details { this.approverEmail = rec.status }
        }
        stub.commentTripLogApproval(request)
    }
}
