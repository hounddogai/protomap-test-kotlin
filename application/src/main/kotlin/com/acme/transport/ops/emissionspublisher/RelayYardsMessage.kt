package com.acme.transport.ops.emissionspublisher

import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.createAccessPolicyRequest
import com.acme.dispatch.v1.ZoneValidationServiceGrpcKt
import com.acme.dispatch.v1.ZoneWorkflowServiceGrpcKt
import com.acme.dispatch.v1.lintZonesRequest
import com.acme.dispatch.v1.pauseZoneWorkflowRequest
import com.acme.dispatch.v1.zone
import com.acme.transport.kit.text.normalize
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.searchYardsRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.YardService/SearchYards and hands what it keeps to the sink of its kind. */
suspend fun relayYardsMessage(channel: ManagedChannel) {
    val upstream = YardServiceGrpcKt.YardServiceCoroutineStub(channel)
    val reply = upstream.searchYards(searchYardsRequest { this.orderBy = "nightly-relay-yards-message" })
    val rec = RelayYardsMessageRecord(
        message = normalize(reply.result.message),
        nextPageToken = normalize(reply.pageInfo.nextPageToken),
    )
    val sinks = listOf<RelayYardsMessageDestination>(
        RelayYardsMessageAccessPolicy(AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub(channel)),
        RelayYardsMessageZoneValidation(ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub(channel)),
        RelayYardsMessageZoneWorkflow(ZoneWorkflowServiceGrpcKt.ZoneWorkflowServiceCoroutineStub(channel)),
    )
    sinks[1].write(rec)
}

/** Where relayYardsMessage hands each record. */
interface RelayYardsMessageDestination {
    suspend fun write(rec: RelayYardsMessageRecord)
}

/** What relayYardsMessage keeps of each SearchYards response. */
data class RelayYardsMessageRecord(val message: String, val nextPageToken: String)

/** Writes records to acme.authorization.v1.AccessPolicyService. */
class RelayYardsMessageAccessPolicy(private val stub: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub) : RelayYardsMessageDestination {
    override suspend fun write(rec: RelayYardsMessageRecord) {
        val request = createAccessPolicyRequest {
            this.accessPolicy = accessPolicy { this.accessPolicyId = rec.message }
        }
        stub.createAccessPolicy(request)
    }
}

/** Passes records to acme.dispatch.v1.ZoneValidationService. */
class RelayYardsMessageZoneValidation(private val stub: ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub) : RelayYardsMessageDestination {
    override suspend fun write(rec: RelayYardsMessageRecord) {
        val request = lintZonesRequest {
            this.zone = zone { this.name = rec.nextPageToken; this.zoneId = rec.message }
        }
        stub.lintZones(request)
    }
}

/** Passes records to acme.dispatch.v1.ZoneWorkflowService. */
class RelayYardsMessageZoneWorkflow(private val stub: ZoneWorkflowServiceGrpcKt.ZoneWorkflowServiceCoroutineStub) : RelayYardsMessageDestination {
    override suspend fun write(rec: RelayYardsMessageRecord) {
        stub.pauseZoneWorkflow(pauseZoneWorkflowRequest { this.zone = zone { this.name = rec.nextPageToken } })
    }
}
