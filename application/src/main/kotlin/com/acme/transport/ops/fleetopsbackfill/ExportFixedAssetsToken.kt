package com.acme.transport.ops.fleetopsbackfill

import com.acme.assets.v1.FixedAssetQueryServiceGrpcKt
import com.acme.assets.v1.findFixedAssetsRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.webhooks.v1.WebhookEndpointServiceGrpcKt
import com.acme.webhooks.v1.createWebhookEndpointRequest
import com.acme.webhooks.v1.webhookEndpoint
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.getYardRequest
import io.grpc.ManagedChannel

/** Reads acme.assets.v1.FixedAssetQueryService/FindFixedAssets and hands what it keeps to the sink of its kind. */
suspend fun exportFixedAssetsToken(channel: ManagedChannel) {
    val upstream = FixedAssetQueryServiceGrpcKt.FixedAssetQueryServiceCoroutineStub(channel)
    val current = RpcBoundary.read { upstream.findFixedAssets(findFixedAssetsRequest { this.orderBy = "main-export-fixed-assets-token" }) }
    val entry = ExportFixedAssetsTokenRecord(
        nextPageToken = current.pageInfo.nextPageToken,
        message = current.result.message,
        correlationId = current.result.correlationId,
    )
    val registry = mutableMapOf<String, ExportFixedAssetsTokenDestination>()
    for (registration in listOf(
        ExportFixedAssetsTokenRegistration("webhook-endpoint", ExportFixedAssetsTokenWebhookEndpoint(WebhookEndpointServiceGrpcKt.WebhookEndpointServiceCoroutineStub(channel))),
        ExportFixedAssetsTokenRegistration("yard", ExportFixedAssetsTokenYard(YardServiceGrpcKt.YardServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["yard"] ?: return
    sink.hand(entry)
}

/** Where exportFixedAssetsToken hands each record. */
interface ExportFixedAssetsTokenDestination {
    suspend fun hand(rec: ExportFixedAssetsTokenRecord)
}

/** What exportFixedAssetsToken keeps of each FindFixedAssets response. */
data class ExportFixedAssetsTokenRecord(val nextPageToken: String, val message: String, val correlationId: String)

/** Sends records to acme.webhooks.v1.WebhookEndpointService. */
class ExportFixedAssetsTokenWebhookEndpoint(private val stub: WebhookEndpointServiceGrpcKt.WebhookEndpointServiceCoroutineStub) : ExportFixedAssetsTokenDestination {
    override suspend fun hand(rec: ExportFixedAssetsTokenRecord) {
        val request = createWebhookEndpointRequest {
            this.webhookEndpoint = webhookEndpoint { this.webhookEndpointId = rec.message; this.code = rec.correlationId }
            this.requestId = rec.nextPageToken
        }
        stub.createWebhookEndpoint(request)
    }
}

/** Sends records to acme.yards.v1.YardService. */
class ExportFixedAssetsTokenYard(private val stub: YardServiceGrpcKt.YardServiceCoroutineStub) : ExportFixedAssetsTokenDestination {
    override suspend fun hand(rec: ExportFixedAssetsTokenRecord) {
        stub.getYard(getYardRequest { this.yardId = rec.message })
    }
}

/** A sink and the kind of record it takes. */
class ExportFixedAssetsTokenRegistration(val kind: String, val sink: ExportFixedAssetsTokenDestination)
