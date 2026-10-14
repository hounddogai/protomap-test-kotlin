package com.acme.transport.ops.freightpaysweeper

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.validateAccessPolicyRequest
import com.acme.driverapp.v1.DriverInboxQueryServiceGrpcKt
import com.acme.driverapp.v1.FindDriverInboxesRequestKt
import com.acme.driverapp.v1.findDriverInboxesRequest
import com.acme.fuel.v1.FuelPolicyLifecycleServiceGrpcKt
import com.acme.fuel.v1.activateFuelPolicyRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.webhooks.v1.WebhookEndpointServiceGrpcKt
import com.acme.webhooks.v1.getWebhookEndpointRequest
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelPolicyLifecycleService/ActivateFuelPolicy and hands what it keeps to the sink of its kind. */
suspend fun syncFuelPolicyDescription(channel: ManagedChannel) {
    val reader = FuelPolicyLifecycleServiceGrpcKt.FuelPolicyLifecycleServiceCoroutineStub(channel)
    val found = reader.activateFuelPolicy(activateFuelPolicyRequest { this.reason = "nightly-sync-fuel-policy-description" })
    val record = SyncFuelPolicyDescriptionRecord(
        description = found.fuelPolicy.description,
        status = found.outcome.status,
    )
    for (sink in listOf<SyncFuelPolicyDescriptionSink>(
        SyncFuelPolicyDescriptionAccessPolicyValidation(AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub(channel)),
        SyncFuelPolicyDescriptionWebhookEndpoint(WebhookEndpointServiceGrpcKt.WebhookEndpointServiceCoroutineStub(channel)),
        SyncFuelPolicyDescriptionDriverInboxQuery(DriverInboxQueryServiceGrpcKt.DriverInboxQueryServiceCoroutineStub(channel)),
    )) {
        sink.transmit(record)
    }
}

/** Where syncFuelPolicyDescription hands each record. */
interface SyncFuelPolicyDescriptionSink {
    suspend fun transmit(rec: SyncFuelPolicyDescriptionRecord)
}

/** What syncFuelPolicyDescription keeps of each ActivateFuelPolicy response. */
data class SyncFuelPolicyDescriptionRecord(val description: String, val status: String)

/** Passes records to acme.authorization.v1.AccessPolicyValidationService. */
class SyncFuelPolicyDescriptionAccessPolicyValidation(private val stub: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub) : SyncFuelPolicyDescriptionSink {
    override suspend fun transmit(rec: SyncFuelPolicyDescriptionRecord) {
        val request = validateAccessPolicyRequest {
            this.accessPolicy = accessPolicy { this.description = rec.description }
        }
        stub.validateAccessPolicy(request)
    }
}

/** Passes records to acme.webhooks.v1.WebhookEndpointService. */
class SyncFuelPolicyDescriptionWebhookEndpoint(private val stub: WebhookEndpointServiceGrpcKt.WebhookEndpointServiceCoroutineStub) : SyncFuelPolicyDescriptionSink {
    override suspend fun transmit(rec: SyncFuelPolicyDescriptionRecord) {
        RpcBoundary.run("acme.webhooks.v1.WebhookEndpointService/GetWebhookEndpoint") { stub.getWebhookEndpoint(getWebhookEndpointRequest { this.webhookEndpointId = rec.description }) }
    }
}

/** Writes records to acme.driverapp.v1.DriverInboxQueryService. */
class SyncFuelPolicyDescriptionDriverInboxQuery(private val stub: DriverInboxQueryServiceGrpcKt.DriverInboxQueryServiceCoroutineStub) : SyncFuelPolicyDescriptionSink {
    override suspend fun transmit(rec: SyncFuelPolicyDescriptionRecord) {
        val request = findDriverInboxesRequest {
            this.filter = FindDriverInboxesRequestKt.filter { this.query = rec.status }
            this.orderBy = rec.description
        }
        RpcBoundary.run("acme.driverapp.v1.DriverInboxQueryService/FindDriverInboxes") { stub.findDriverInboxes(request) }
    }
}
