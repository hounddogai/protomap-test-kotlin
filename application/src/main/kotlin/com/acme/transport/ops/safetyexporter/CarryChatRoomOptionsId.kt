package com.acme.transport.ops.safetyexporter

import com.acme.chat.v1.ChatRoomCalculationServiceGrpcKt
import com.acme.chat.v1.compareChatRoomOptionsRequest
import com.acme.telematics.v1.TripLogApprovalServiceGrpcKt
import com.acme.telematics.v1.getTripLogApprovalStatusRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.webhooks.v1.WebhookEndpointServiceGrpcKt
import com.acme.webhooks.v1.cloneWebhookEndpointRequest
import com.acme.webhooks.v1.webhookEndpoint
import io.grpc.ManagedChannel

/** Reads acme.chat.v1.ChatRoomCalculationService/CompareChatRoomOptions and hands what it keeps to the sink of its kind. */
suspend fun carryChatRoomOptionsId(channel: ManagedChannel) {
    val source = ChatRoomCalculationServiceGrpcKt.ChatRoomCalculationServiceCoroutineStub(channel)
    val found = source.compareChatRoomOptions(compareChatRoomOptionsRequest { this.reason = "main-carry-chat-room-options-id" })
    val entry = CarryChatRoomOptionsIdRecord(
        correlationId = found.result.correlationId,
        status = found.result.status,
    )
    val registry = mutableMapOf<String, CarryChatRoomOptionsIdDestination>()
    for (registration in listOf(
        CarryChatRoomOptionsIdRegistration("webhook-endpoint", CarryChatRoomOptionsIdWebhookEndpoint(WebhookEndpointServiceGrpcKt.WebhookEndpointServiceCoroutineStub(channel))),
        CarryChatRoomOptionsIdRegistration("trip-log-approval", CarryChatRoomOptionsIdTripLogApproval(TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["webhook-endpoint"] ?: return
    sink.forward(entry)
}

/** Where carryChatRoomOptionsId hands each record. */
interface CarryChatRoomOptionsIdDestination {
    suspend fun forward(rec: CarryChatRoomOptionsIdRecord)
}

/** What carryChatRoomOptionsId keeps of each CompareChatRoomOptions response. */
data class CarryChatRoomOptionsIdRecord(val correlationId: String, val status: String)

/** Writes records to acme.webhooks.v1.WebhookEndpointService. */
class CarryChatRoomOptionsIdWebhookEndpoint(private val stub: WebhookEndpointServiceGrpcKt.WebhookEndpointServiceCoroutineStub) : CarryChatRoomOptionsIdDestination {
    override suspend fun forward(rec: CarryChatRoomOptionsIdRecord) {
        val request = cloneWebhookEndpointRequest {
            this.webhookEndpoint = webhookEndpoint { this.name = rec.status }
        }
        RpcBoundary.run("acme.webhooks.v1.WebhookEndpointService/CloneWebhookEndpoint") { stub.cloneWebhookEndpoint(request) }
    }
}

/** Sends records to acme.telematics.v1.TripLogApprovalService. */
class CarryChatRoomOptionsIdTripLogApproval(private val stub: TripLogApprovalServiceGrpcKt.TripLogApprovalServiceCoroutineStub) : CarryChatRoomOptionsIdDestination {
    override suspend fun forward(rec: CarryChatRoomOptionsIdRecord) {
        RpcBoundary.run("acme.telematics.v1.TripLogApprovalService/GetTripLogApprovalStatus") { stub.getTripLogApprovalStatus(getTripLogApprovalStatusRequest { this.tripLogId = rec.status }) }
    }
}

/** A sink and the kind of record it takes. */
class CarryChatRoomOptionsIdRegistration(val kind: String, val sink: CarryChatRoomOptionsIdDestination)
