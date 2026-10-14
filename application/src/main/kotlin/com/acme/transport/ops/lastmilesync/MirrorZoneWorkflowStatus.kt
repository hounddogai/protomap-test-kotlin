package com.acme.transport.ops.lastmilesync

import com.acme.customers.v1.CustomerLifecycleServiceGrpcKt
import com.acme.customers.v1.HouseholdWorkflowServiceGrpcKt
import com.acme.customers.v1.household
import com.acme.customers.v1.pauseHouseholdWorkflowRequest
import com.acme.customers.v1.suspendCustomerRequest
import com.acme.dispatch.v1.ZoneWorkflowServiceGrpcKt
import com.acme.dispatch.v1.startZoneWorkflowRequest
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.normalizeFeatureFlagRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.dispatch.v1.ZoneWorkflowService/StartZoneWorkflow and hands what it keeps to the sink of its kind. */
suspend fun mirrorZoneWorkflowStatus(channel: ManagedChannel) {
    val upstream = ZoneWorkflowServiceGrpcKt.ZoneWorkflowServiceCoroutineStub(channel)
    val reply = upstream.startZoneWorkflow(startZoneWorkflowRequest { this.zoneId = "current-mirror-zone-workflow-status" })
    val record = MirrorZoneWorkflowStatusRecord(
        status = reply.outcome.status,
        correlationId = reply.result.correlationId,
        message = reply.outcome.message,
        name = normalize(reply.zone.name),
    )
    deliverMirrorZoneWorkflowStatus("customer-lifecycle", record, channel)
}

/** Where mirrorZoneWorkflowStatus hands each record. */
interface MirrorZoneWorkflowStatusWriter {
    suspend fun forward(rec: MirrorZoneWorkflowStatusRecord)
}

/** What mirrorZoneWorkflowStatus keeps of each StartZoneWorkflow response. */
data class MirrorZoneWorkflowStatusRecord(val status: String, val correlationId: String, val message: String, val name: String)

/** Passes records to acme.customers.v1.CustomerLifecycleService. */
class MirrorZoneWorkflowStatusCustomerLifecycle(private val stub: CustomerLifecycleServiceGrpcKt.CustomerLifecycleServiceCoroutineStub) : MirrorZoneWorkflowStatusWriter {
    override suspend fun forward(rec: MirrorZoneWorkflowStatusRecord) {
        stub.suspendCustomer(suspendCustomerRequest { this.customerId = rec.status; this.reason = rec.message })
    }
}

/** Passes records to acme.customers.v1.HouseholdWorkflowService. */
class MirrorZoneWorkflowStatusHouseholdWorkflow(private val stub: HouseholdWorkflowServiceGrpcKt.HouseholdWorkflowServiceCoroutineStub) : MirrorZoneWorkflowStatusWriter {
    override suspend fun forward(rec: MirrorZoneWorkflowStatusRecord) {
        val request = pauseHouseholdWorkflowRequest {
            this.household = household { this.name = rec.message; this.householdId = rec.status }
            this.reason = rec.correlationId
        }
        RpcBoundary.run("acme.customers.v1.HouseholdWorkflowService/PauseHouseholdWorkflow") { stub.pauseHouseholdWorkflow(request) }
    }
}

/** Hands records to acme.featureflags.v1.FeatureFlagValidationService. */
class MirrorZoneWorkflowStatusFeatureFlagValidation(private val stub: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub) : MirrorZoneWorkflowStatusWriter {
    override suspend fun forward(rec: MirrorZoneWorkflowStatusRecord) {
        stub.normalizeFeatureFlag(normalizeFeatureFlagRequest { this.reason = rec.correlationId })
    }
}

/** Only logs, for kinds no service takes yet. */
class MirrorZoneWorkflowStatusLogOnly : MirrorZoneWorkflowStatusWriter {
    override suspend fun forward(rec: MirrorZoneWorkflowStatusRecord) {
        LoggerFactory.getLogger("MirrorZoneWorkflowStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverMirrorZoneWorkflowStatus(kind: String, rec: MirrorZoneWorkflowStatusRecord, channel: ManagedChannel) {
    var sink: MirrorZoneWorkflowStatusWriter = MirrorZoneWorkflowStatusLogOnly()
    when (kind) {
        "customer-lifecycle" -> sink = MirrorZoneWorkflowStatusCustomerLifecycle(CustomerLifecycleServiceGrpcKt.CustomerLifecycleServiceCoroutineStub(channel))
        "household-workflow" -> sink = MirrorZoneWorkflowStatusHouseholdWorkflow(HouseholdWorkflowServiceGrpcKt.HouseholdWorkflowServiceCoroutineStub(channel))
        "feature-flag-validation" -> sink = MirrorZoneWorkflowStatusFeatureFlagValidation(FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
