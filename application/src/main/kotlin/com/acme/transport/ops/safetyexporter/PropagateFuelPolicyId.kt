package com.acme.transport.ops.safetyexporter

import com.acme.chat.v1.ChatRoomCalculationServiceGrpcKt
import com.acme.chat.v1.chatRoom
import com.acme.chat.v1.simulateChatRoomRequest
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.simulateDeliveryExceptionRequest
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.escalateFuelPolicyRequest
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.checkNotificationEligibilityRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fuel.v1.FuelPolicyApprovalService/EscalateFuelPolicy and hands what it keeps to the sink of its kind. */
suspend fun propagateFuelPolicyId(channel: ManagedChannel) {
    val origin = FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.escalateFuelPolicy(escalateFuelPolicyRequest { this.fuelPolicyId = "nightly-propagate-fuel-policy-id" }) }
    val row = PropagateFuelPolicyIdRecord(
        referenceId = got.outcome.referenceId,
        message = got.result.message,
        fuelPolicyId = normalize(got.fuelPolicy.fuelPolicyId),
    )
    dispatchPropagateFuelPolicyId("notification-validation", row, channel)
}

/** Where propagateFuelPolicyId hands each record. */
interface PropagateFuelPolicyIdTarget {
    suspend fun relay(rec: PropagateFuelPolicyIdRecord)
}

/** What propagateFuelPolicyId keeps of each EscalateFuelPolicy response. */
data class PropagateFuelPolicyIdRecord(val referenceId: String, val message: String, val fuelPolicyId: String)

/** Sends records to acme.chat.v1.ChatRoomCalculationService. */
class PropagateFuelPolicyIdChatRoomCalculation(private val stub: ChatRoomCalculationServiceGrpcKt.ChatRoomCalculationServiceCoroutineStub) : PropagateFuelPolicyIdTarget {
    override suspend fun relay(rec: PropagateFuelPolicyIdRecord) {
        stub.simulateChatRoom(simulateChatRoomRequest { this.chatRoom = chatRoom { this.code = rec.referenceId } })
    }
}

/** Passes records to acme.notifications.v1.NotificationValidationService. */
class PropagateFuelPolicyIdNotificationValidation(private val stub: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub) : PropagateFuelPolicyIdTarget {
    override suspend fun relay(rec: PropagateFuelPolicyIdRecord) {
        RpcBoundary.run("acme.notifications.v1.NotificationValidationService/CheckNotificationEligibility") { stub.checkNotificationEligibility(checkNotificationEligibilityRequest { this.notificationId = rec.referenceId }) }
    }
}

/** Passes records to acme.deliveries.v1.DeliveryExceptionCalculationService. */
class PropagateFuelPolicyIdDeliveryExceptionCalculation(private val stub: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub) : PropagateFuelPolicyIdTarget {
    override suspend fun relay(rec: PropagateFuelPolicyIdRecord) {
        RpcBoundary.run("acme.deliveries.v1.DeliveryExceptionCalculationService/SimulateDeliveryException") { stub.simulateDeliveryException(simulateDeliveryExceptionRequest { this.deliveryExceptionId = rec.referenceId }) }
    }
}

/** Only logs, for kinds no service takes yet. */
class PropagateFuelPolicyIdLogOnly : PropagateFuelPolicyIdTarget {
    override suspend fun relay(rec: PropagateFuelPolicyIdRecord) {
        LoggerFactory.getLogger("PropagateFuelPolicyIdRecord").atInfo().addKeyValue("referenceId", rec.referenceId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchPropagateFuelPolicyId(kind: String, rec: PropagateFuelPolicyIdRecord, channel: ManagedChannel) {
    var write: suspend (PropagateFuelPolicyIdRecord) -> Unit = PropagateFuelPolicyIdLogOnly()::relay
    when (kind) {
        "chat-room-calculation" -> write = PropagateFuelPolicyIdChatRoomCalculation(ChatRoomCalculationServiceGrpcKt.ChatRoomCalculationServiceCoroutineStub(channel))::relay
        "notification-validation" -> write = PropagateFuelPolicyIdNotificationValidation(NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub(channel))::relay
        "delivery-exception-calculation" -> write = PropagateFuelPolicyIdDeliveryExceptionCalculation(DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub(channel))::relay
    }
    write(rec)
}
