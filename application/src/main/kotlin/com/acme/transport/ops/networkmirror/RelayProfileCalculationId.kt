package com.acme.transport.ops.networkmirror

import com.acme.driverapp.v1.DriverInboxServiceGrpcKt
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.updateDriverInboxRequest
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.explainNotificationDecisionRequest
import com.acme.notifications.v1.notification
import com.acme.personalization.v1.ProfileCalculationServiceGrpcKt
import com.acme.personalization.v1.calculateProfileRequest
import com.acme.personalization.v1.explainProfileCalculationRequest
import com.acme.personalization.v1.profile
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.personalization.v1.ProfileCalculationService/ExplainProfileCalculation and hands what it keeps to the sink of its kind. */
suspend fun relayProfileCalculationId(channel: ManagedChannel) {
    val origin = ProfileCalculationServiceGrpcKt.ProfileCalculationServiceCoroutineStub(channel)
    val current = origin.explainProfileCalculation(explainProfileCalculationRequest { this.profileId = "current-relay-profile-calculation-id" })
    val entry = RelayProfileCalculationIdRecord(
        profileId = current.profile.profileId,
        status = normalize(current.outcome.status),
        correlationId = normalize(current.result.correlationId),
        message = current.result.message,
    )
    val sinks = mapOf<String, RelayProfileCalculationIdWriter>(
        "notification-validation" to RelayProfileCalculationIdNotificationValidation(NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub(channel)),
        "driver-inbox" to RelayProfileCalculationIdDriverInbox(DriverInboxServiceGrpcKt.DriverInboxServiceCoroutineStub(channel)),
        "profile-calculation" to RelayProfileCalculationIdProfileCalculation(ProfileCalculationServiceGrpcKt.ProfileCalculationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "notification-validation") continue
        sinks[kind]?.hand(entry)
    }
}

/** Where relayProfileCalculationId hands each record. */
interface RelayProfileCalculationIdWriter {
    suspend fun hand(rec: RelayProfileCalculationIdRecord)
}

/** What relayProfileCalculationId keeps of each ExplainProfileCalculation response. */
data class RelayProfileCalculationIdRecord(val profileId: String, val status: String, val correlationId: String, val message: String)

/** Sends records to acme.notifications.v1.NotificationValidationService. */
class RelayProfileCalculationIdNotificationValidation(private val stub: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub) : RelayProfileCalculationIdWriter {
    override suspend fun hand(rec: RelayProfileCalculationIdRecord) {
        val request = explainNotificationDecisionRequest {
            this.notification = notification { this.subject = rec.status }
        }
        stub.explainNotificationDecision(request)
    }
}

/** Hands records to acme.driverapp.v1.DriverInboxService. */
class RelayProfileCalculationIdDriverInbox(private val stub: DriverInboxServiceGrpcKt.DriverInboxServiceCoroutineStub) : RelayProfileCalculationIdWriter {
    override suspend fun hand(rec: RelayProfileCalculationIdRecord) {
        val request = updateDriverInboxRequest {
            this.driverInbox = driverInbox { this.number = rec.correlationId; this.notes = rec.profileId; this.driverInboxId = rec.message }
        }
        stub.updateDriverInbox(request)
    }
}

/** Writes records to acme.personalization.v1.ProfileCalculationService. */
class RelayProfileCalculationIdProfileCalculation(private val stub: ProfileCalculationServiceGrpcKt.ProfileCalculationServiceCoroutineStub) : RelayProfileCalculationIdWriter {
    override suspend fun hand(rec: RelayProfileCalculationIdRecord) {
        val request = calculateProfileRequest {
            this.profileId = rec.status
            this.profile = profile { this.profileId = rec.profileId }
            this.reason = rec.correlationId
        }
        RpcBoundary.run("acme.personalization.v1.ProfileCalculationService/CalculateProfile") { stub.calculateProfile(request) }
    }
}
