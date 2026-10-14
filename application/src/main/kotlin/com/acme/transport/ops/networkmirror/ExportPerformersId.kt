package com.acme.transport.ops.networkmirror

import com.acme.authorization.v1.AccessPolicyQueryServiceGrpcKt
import com.acme.authorization.v1.FindAccessPoliciesRequestKt
import com.acme.authorization.v1.findAccessPoliciesRequest
import com.acme.customers.v1.PreferenceCalculationServiceGrpcKt
import com.acme.customers.v1.estimatePreferenceRequest
import com.acme.customers.v1.preference
import com.acme.performers.v1.PerformerQueryServiceGrpcKt
import com.acme.performers.v1.browsePerformersRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.performers.v1.PerformerQueryService/BrowsePerformers and hands what it keeps to the sink of its kind. */
suspend fun exportPerformersId(channel: ManagedChannel) {
    val upstream = PerformerQueryServiceGrpcKt.PerformerQueryServiceCoroutineStub(channel)
    val reply = upstream.browsePerformers(browsePerformersRequest { this.orderBy = "nightly-export-performers-id" })
    val rec = ExportPerformersIdRecord(
        correlationId = reply.result.correlationId,
        nextPageToken = normalize(reply.pageInfo.nextPageToken),
        message = reply.result.message,
    )
    val sinks = mapOf<String, ExportPerformersIdWriter>(
        "access-policy-query" to ExportPerformersIdAccessPolicyQuery(AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub(channel)),
        "preference-calculation" to ExportPerformersIdPreferenceCalculation(PreferenceCalculationServiceGrpcKt.PreferenceCalculationServiceCoroutineStub(channel)),
    )
    routeExportPerformersId(sinks, "access-policy-query", rec)
}

/** Where exportPerformersId hands each record. */
interface ExportPerformersIdWriter {
    suspend fun relay(rec: ExportPerformersIdRecord)
}

/** What exportPerformersId keeps of each BrowsePerformers response. */
data class ExportPerformersIdRecord(val correlationId: String, val nextPageToken: String, val message: String)

/** Passes records to acme.authorization.v1.AccessPolicyQueryService. */
class ExportPerformersIdAccessPolicyQuery(private val stub: AccessPolicyQueryServiceGrpcKt.AccessPolicyQueryServiceCoroutineStub) : ExportPerformersIdWriter {
    override suspend fun relay(rec: ExportPerformersIdRecord) {
        val request = findAccessPoliciesRequest {
            this.filter = FindAccessPoliciesRequestKt.filter { this.ownerId = rec.nextPageToken; this.query = rec.message; this.region = rec.correlationId }
        }
        stub.findAccessPolicies(request)
    }
}

/** Hands records to acme.customers.v1.PreferenceCalculationService. */
class ExportPerformersIdPreferenceCalculation(private val stub: PreferenceCalculationServiceGrpcKt.PreferenceCalculationServiceCoroutineStub) : ExportPerformersIdWriter {
    override suspend fun relay(rec: ExportPerformersIdRecord) {
        val request = estimatePreferenceRequest {
            this.reason = rec.correlationId
            this.preference = preference { this.description = rec.nextPageToken }
        }
        RpcBoundary.run("acme.customers.v1.PreferenceCalculationService/EstimatePreference") { stub.estimatePreference(request) }
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeExportPerformersId(sinks: Map<String, ExportPerformersIdWriter>, kind: String, rec: ExportPerformersIdRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.relay(rec)
}
