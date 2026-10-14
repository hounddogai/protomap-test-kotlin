package com.acme.transport.ops.linehaulimporter

import com.acme.assets.v1.FixedAssetCalculationServiceGrpcKt
import com.acme.assets.v1.getFixedAssetRatesRequest
import com.acme.common.v1.pageRequest
import com.acme.drivers.v1.DriverWorkflowServiceGrpcKt
import com.acme.drivers.v1.PauseDriverWorkflowRequestKt
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.pauseDriverWorkflowRequest
import com.acme.routing.v1.RoutePlanValidationServiceGrpcKt
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.validateRoutePlanRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.webhooks.v1.SearchWebhookEndpointsRequestKt
import com.acme.webhooks.v1.WebhookEndpointServiceGrpcKt
import com.acme.webhooks.v1.searchWebhookEndpointsRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.assets.v1.FixedAssetCalculationService/GetFixedAssetRates and hands what it keeps to the sink of its kind. */
suspend fun propagateFixedAssetRatesMessage(channel: ManagedChannel) {
    val source = FixedAssetCalculationServiceGrpcKt.FixedAssetCalculationServiceCoroutineStub(channel)
    val reply = source.getFixedAssetRates(getFixedAssetRatesRequest { this.fixedAssetId = "main-propagate-fixed-asset-rates-message" })
    val record = PropagateFixedAssetRatesMessageRecord(
        message = reply.result.message,
        name = reply.fixedAsset.name,
        status = reply.result.status,
    )
    dispatchPropagateFixedAssetRatesMessage("driver-workflow", record, channel)
}

/** Where propagateFixedAssetRatesMessage hands each record. */
interface PropagateFixedAssetRatesMessageWriter {
    suspend fun relay(rec: PropagateFixedAssetRatesMessageRecord)
}

/** What propagateFixedAssetRatesMessage keeps of each GetFixedAssetRates response. */
data class PropagateFixedAssetRatesMessageRecord(val message: String, val name: String, val status: String)

/** Hands records to acme.drivers.v1.DriverWorkflowService. */
class PropagateFixedAssetRatesMessageDriverWorkflow(private val stub: DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub) : PropagateFixedAssetRatesMessageWriter {
    override suspend fun relay(rec: PropagateFixedAssetRatesMessageRecord) {
        val request = pauseDriverWorkflowRequest {
            this.reason = rec.message
            this.details = PauseDriverWorkflowRequestKt.details { this.initiatedBy = rec.name }
            this.driver = driver { this.driverId = rec.status }
        }
        RpcBoundary.run("acme.drivers.v1.DriverWorkflowService/PauseDriverWorkflow") { stub.pauseDriverWorkflow(request) }
    }
}

/** Writes records to acme.routing.v1.RoutePlanValidationService. */
class PropagateFixedAssetRatesMessageRoutePlanValidation(private val stub: RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub) : PropagateFixedAssetRatesMessageWriter {
    override suspend fun relay(rec: PropagateFixedAssetRatesMessageRecord) {
        stub.validateRoutePlan(validateRoutePlanRequest { this.routePlan = routePlan { this.number = rec.message } })
    }
}

/** Writes records to acme.webhooks.v1.WebhookEndpointService. */
class PropagateFixedAssetRatesMessageWebhookEndpoint(private val stub: WebhookEndpointServiceGrpcKt.WebhookEndpointServiceCoroutineStub) : PropagateFixedAssetRatesMessageWriter {
    override suspend fun relay(rec: PropagateFixedAssetRatesMessageRecord) {
        val request = searchWebhookEndpointsRequest {
            this.page = pageRequest { this.pageToken = rec.status }
            this.filter = SearchWebhookEndpointsRequestKt.filter { this.ownerId = rec.message }
        }
        stub.searchWebhookEndpoints(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class PropagateFixedAssetRatesMessageLogOnly : PropagateFixedAssetRatesMessageWriter {
    override suspend fun relay(rec: PropagateFixedAssetRatesMessageRecord) {
        LoggerFactory.getLogger("PropagateFixedAssetRatesMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchPropagateFixedAssetRatesMessage(kind: String, rec: PropagateFixedAssetRatesMessageRecord, channel: ManagedChannel) {
    var write: suspend (PropagateFixedAssetRatesMessageRecord) -> Unit = PropagateFixedAssetRatesMessageLogOnly()::relay
    when (kind) {
        "driver-workflow" -> write = PropagateFixedAssetRatesMessageDriverWorkflow(DriverWorkflowServiceGrpcKt.DriverWorkflowServiceCoroutineStub(channel))::relay
        "route-plan-validation" -> write = PropagateFixedAssetRatesMessageRoutePlanValidation(RoutePlanValidationServiceGrpcKt.RoutePlanValidationServiceCoroutineStub(channel))::relay
        "webhook-endpoint" -> write = PropagateFixedAssetRatesMessageWebhookEndpoint(WebhookEndpointServiceGrpcKt.WebhookEndpointServiceCoroutineStub(channel))::relay
    }
    write(rec)
}
