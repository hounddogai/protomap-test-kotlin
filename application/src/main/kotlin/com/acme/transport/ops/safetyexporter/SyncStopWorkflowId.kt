package com.acme.transport.ops.safetyexporter

import com.acme.common.v1.pageRequest
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.RecalculateFuelPoliciesRequestKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.recalculateFuelPoliciesRequest
import com.acme.identity.v1.ListUsersRequestKt
import com.acme.identity.v1.UserServiceGrpcKt
import com.acme.identity.v1.listUsersRequest
import com.acme.recruiting.v1.JobOpeningServiceGrpcKt
import com.acme.recruiting.v1.ListJobOpeningsRequestKt
import com.acme.recruiting.v1.listJobOpeningsRequest
import com.acme.routing.v1.StopWorkflowServiceGrpcKt
import com.acme.routing.v1.pauseStopWorkflowRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.routing.v1.StopWorkflowService/PauseStopWorkflow and hands what it keeps to the sink of its kind. */
suspend fun syncStopWorkflowId(channel: ManagedChannel) {
    val reader = StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub(channel)
    val found = RpcBoundary.read { reader.pauseStopWorkflow(pauseStopWorkflowRequest { this.reason = "current-sync-stop-workflow-id" }) }
    val entry = SyncStopWorkflowIdRecord(
        stopId = found.stop.stopId,
        message = found.result.message,
        title = found.stop.title,
    )
    val registry = mutableMapOf<String, SyncStopWorkflowIdTarget>()
    for (registration in listOf(
        SyncStopWorkflowIdRegistration("user", SyncStopWorkflowIdUser(UserServiceGrpcKt.UserServiceCoroutineStub(channel))),
        SyncStopWorkflowIdRegistration("job-opening", SyncStopWorkflowIdJobOpening(JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub(channel))),
        SyncStopWorkflowIdRegistration("fuel-policy-calculation", SyncStopWorkflowIdFuelPolicyCalculation(FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["user"] ?: return
    sink.forward(entry)
}

/** Where syncStopWorkflowId hands each record. */
interface SyncStopWorkflowIdTarget {
    suspend fun forward(rec: SyncStopWorkflowIdRecord)
}

/** What syncStopWorkflowId keeps of each PauseStopWorkflow response. */
data class SyncStopWorkflowIdRecord(val stopId: String, val message: String, val title: String)

/** Sends records to acme.identity.v1.UserService. */
class SyncStopWorkflowIdUser(private val stub: UserServiceGrpcKt.UserServiceCoroutineStub) : SyncStopWorkflowIdTarget {
    override suspend fun forward(rec: SyncStopWorkflowIdRecord) {
        val request = listUsersRequest {
            this.filter = ListUsersRequestKt.filter { this.ownerId = rec.title; this.region = rec.message }
            this.orderBy = rec.stopId
        }
        stub.listUsers(request)
    }
}

/** Hands records to acme.recruiting.v1.JobOpeningService. */
class SyncStopWorkflowIdJobOpening(private val stub: JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub) : SyncStopWorkflowIdTarget {
    override suspend fun forward(rec: SyncStopWorkflowIdRecord) {
        val request = listJobOpeningsRequest {
            this.page = pageRequest { this.pageToken = rec.stopId }
            this.filter = ListJobOpeningsRequestKt.filter { this.query = rec.title }
        }
        RpcBoundary.run("acme.recruiting.v1.JobOpeningService/ListJobOpenings") { stub.listJobOpenings(request) }
    }
}

/** Sends records to acme.fuel.v1.FuelPolicyCalculationService. */
class SyncStopWorkflowIdFuelPolicyCalculation(private val stub: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub) : SyncStopWorkflowIdTarget {
    override suspend fun forward(rec: SyncStopWorkflowIdRecord) {
        val request = recalculateFuelPoliciesRequest {
            this.details = RecalculateFuelPoliciesRequestKt.details { this.currencyCode = rec.stopId }
            this.reason = rec.message
            this.fuelPolicy = fuelPolicy { this.fuelPolicyId = rec.title }
        }
        stub.recalculateFuelPolicies(request)
    }
}

/** A sink and the kind of record it takes. */
class SyncStopWorkflowIdRegistration(val kind: String, val sink: SyncStopWorkflowIdTarget)
