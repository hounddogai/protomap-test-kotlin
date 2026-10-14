package com.acme.transport.ops.lastmilebackfill

import com.acme.customers.v1.CustomerSegmentSyncServiceGrpcKt
import com.acme.customers.v1.PullCustomerSegmentsRequestKt
import com.acme.customers.v1.pullCustomerSegmentsRequest
import com.acme.fuel.v1.FuelPolicyHistoryServiceGrpcKt
import com.acme.fuel.v1.recordFuelPolicyVersionRequest
import com.acme.identity.v1.ListUsersRequestKt
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.listUsersRequest
import com.acme.patients.v1.BrowsePatientsRequestKt
import com.acme.patients.v1.PatientQueryServiceGrpcKt
import com.acme.patients.v1.browsePatientsRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.CountDocksRequestKt
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.countDocksRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fuel.v1.FuelPolicyHistoryService/RecordFuelPolicyVersion and hands what it keeps to the sink of its kind. */
suspend fun replayFuelPolicyVersionMessage(channel: ManagedChannel) {
    val upstream = FuelPolicyHistoryServiceGrpcKt.FuelPolicyHistoryServiceCoroutineStub(channel)
    val reply = upstream.recordFuelPolicyVersion(recordFuelPolicyVersionRequest { this.reason = "main-replay-fuel-policy-version-message" })
    val entry = ReplayFuelPolicyVersionMessageRecord(
        message = reply.outcome.message,
        correlationId = reply.result.correlationId,
    )
    dispatchReplayFuelPolicyVersionMessage("patient-query", entry, channel)
}

/** Where replayFuelPolicyVersionMessage hands each record. */
interface ReplayFuelPolicyVersionMessageWriter {
    suspend fun write(rec: ReplayFuelPolicyVersionMessageRecord)
}

/** What replayFuelPolicyVersionMessage keeps of each RecordFuelPolicyVersion response. */
data class ReplayFuelPolicyVersionMessageRecord(val message: String, val correlationId: String)

/** Sends records to acme.patients.v1.PatientQueryService. */
class ReplayFuelPolicyVersionMessagePatientQuery(private val stub: PatientQueryServiceGrpcKt.PatientQueryServiceCoroutineStub) : ReplayFuelPolicyVersionMessageWriter {
    override suspend fun write(rec: ReplayFuelPolicyVersionMessageRecord) {
        val request = browsePatientsRequest {
            this.filter = BrowsePatientsRequestKt.filter { this.query = rec.message; this.ownerId = rec.correlationId }
        }
        stub.browsePatients(request)
    }
}

/** Writes records to acme.identity.v1.UserService. */
class ReplayFuelPolicyVersionMessageUser(private val stub: UserServiceGrpcKt.UserServiceCoroutineStub) : ReplayFuelPolicyVersionMessageWriter {
    override suspend fun write(rec: ReplayFuelPolicyVersionMessageRecord) {
        val request = listUsersRequest {
            this.orderBy = rec.correlationId
            this.filter = ListUsersRequestKt.filter { this.ownerId = rec.message }
        }
        RpcBoundary.run("acme.identity.v1.UserService/ListUsers") { stub.listUsers(request) }
    }
}

/** Writes records to acme.yards.v1.DockQueryService. */
class ReplayFuelPolicyVersionMessageDockQuery(private val stub: DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub) : ReplayFuelPolicyVersionMessageWriter {
    override suspend fun write(rec: ReplayFuelPolicyVersionMessageRecord) {
        val request = countDocksRequest {
            this.filter = CountDocksRequestKt.filter { this.query = rec.correlationId; this.ownerId = rec.message }
        }
        stub.countDocks(request)
    }
}

/** Hands records to acme.customers.v1.CustomerSegmentSyncService. */
class ReplayFuelPolicyVersionMessageCustomerSegmentSync(private val stub: CustomerSegmentSyncServiceGrpcKt.CustomerSegmentSyncServiceCoroutineStub) : ReplayFuelPolicyVersionMessageWriter {
    override suspend fun write(rec: ReplayFuelPolicyVersionMessageRecord) {
        val request = pullCustomerSegmentsRequest {
            this.customerSegmentId = rec.message
            this.details = PullCustomerSegmentsRequestKt.details { this.resolution = rec.correlationId }
        }
        stub.pullCustomerSegments(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class ReplayFuelPolicyVersionMessageLogOnly : ReplayFuelPolicyVersionMessageWriter {
    override suspend fun write(rec: ReplayFuelPolicyVersionMessageRecord) {
        LoggerFactory.getLogger("ReplayFuelPolicyVersionMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchReplayFuelPolicyVersionMessage(kind: String, rec: ReplayFuelPolicyVersionMessageRecord, channel: ManagedChannel) {
    var write: suspend (ReplayFuelPolicyVersionMessageRecord) -> Unit = ReplayFuelPolicyVersionMessageLogOnly()::write
    when (kind) {
        "patient-query" -> write = ReplayFuelPolicyVersionMessagePatientQuery(PatientQueryServiceGrpcKt.PatientQueryServiceCoroutineStub(channel))::write
        "user" -> write = ReplayFuelPolicyVersionMessageUser(UserServiceGrpcKt.UserServiceCoroutineStub(channel))::write
        "dock-query" -> write = ReplayFuelPolicyVersionMessageDockQuery(DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub(channel))::write
        "customer-segment-sync" -> write = ReplayFuelPolicyVersionMessageCustomerSegmentSync(CustomerSegmentSyncServiceGrpcKt.CustomerSegmentSyncServiceCoroutineStub(channel))::write
    }
    write(rec)
}
