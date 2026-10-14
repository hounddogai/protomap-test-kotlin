package com.acme.transport.ops.freightpayreplicator

import com.acme.dispatch.v1.ZoneValidationServiceGrpcKt
import com.acme.dispatch.v1.scoreZoneRequest
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.getDriverHomeApprovalStatusRequest
import com.acme.notifications.v1.ExplainNotificationDecisionRequestKt
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.explainNotificationDecisionRequest
import com.acme.notifications.v1.notification
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.createVenueRequest
import io.grpc.ManagedChannel

/** Reads acme.venues.v1.VenueService/CreateVenue and hands what it keeps to the sink of its kind. */
suspend fun mirrorVenueStatus(channel: ManagedChannel) {
    val source = VenueServiceGrpcKt.VenueServiceCoroutineStub(channel)
    val found = source.createVenue(createVenueRequest { this.requestId = "default-mirror-venue-status" })
    val record = MirrorVenueStatusRecord(
        status = normalize(found.result.status),
        correlationId = found.result.correlationId,
    )
    val sinks = mapOf<String, MirrorVenueStatusDestination>(
        "driver-home-approval" to MirrorVenueStatusDriverHomeApproval(DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub(channel)),
        "zone-validation" to MirrorVenueStatusZoneValidation(ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub(channel)),
        "notification-validation" to MirrorVenueStatusNotificationValidation(NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub(channel)),
    )
    routeMirrorVenueStatus(sinks, "zone-validation", record)
}

/** Where mirrorVenueStatus hands each record. */
interface MirrorVenueStatusDestination {
    suspend fun transmit(rec: MirrorVenueStatusRecord)
}

/** What mirrorVenueStatus keeps of each CreateVenue response. */
data class MirrorVenueStatusRecord(val status: String, val correlationId: String)

/** Passes records to acme.driverapp.v1.DriverHomeApprovalService. */
class MirrorVenueStatusDriverHomeApproval(private val stub: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub) : MirrorVenueStatusDestination {
    override suspend fun transmit(rec: MirrorVenueStatusRecord) {
        RpcBoundary.run("acme.driverapp.v1.DriverHomeApprovalService/GetDriverHomeApprovalStatus") { stub.getDriverHomeApprovalStatus(getDriverHomeApprovalStatusRequest { this.driverHomeId = rec.correlationId }) }
    }
}

/** Hands records to acme.dispatch.v1.ZoneValidationService. */
class MirrorVenueStatusZoneValidation(private val stub: ZoneValidationServiceGrpcKt.ZoneValidationServiceCoroutineStub) : MirrorVenueStatusDestination {
    override suspend fun transmit(rec: MirrorVenueStatusRecord) {
        RpcBoundary.run("acme.dispatch.v1.ZoneValidationService/ScoreZone") { stub.scoreZone(scoreZoneRequest { this.zoneId = rec.status }) }
    }
}

/** Writes records to acme.notifications.v1.NotificationValidationService. */
class MirrorVenueStatusNotificationValidation(private val stub: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub) : MirrorVenueStatusDestination {
    override suspend fun transmit(rec: MirrorVenueStatusRecord) {
        val request = explainNotificationDecisionRequest {
            this.details = ExplainNotificationDecisionRequestKt.details { this.locale = rec.correlationId }
            this.notification = notification { this.notificationId = rec.status }
        }
        stub.explainNotificationDecision(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeMirrorVenueStatus(sinks: Map<String, MirrorVenueStatusDestination>, kind: String, rec: MirrorVenueStatusRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.transmit(rec)
}
