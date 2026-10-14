package com.acme.transport.ops.safetyexporter

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.PreviewAuditEventRequestKt
import com.acme.audit.v1.auditEvent
import com.acme.audit.v1.previewAuditEventRequest
import com.acme.driverapp.v1.AcknowledgeDriverHomeEventRequestKt
import com.acme.driverapp.v1.DriverHomeIntegrationServiceGrpcKt
import com.acme.driverapp.v1.acknowledgeDriverHomeEventRequest
import com.acme.notifications.v1.NotificationValidationServiceGrpcKt
import com.acme.notifications.v1.getNotificationRulesRequest
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.quoteRouteRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.workplaces.v1.BuildingQueryServiceGrpcKt
import com.acme.workplaces.v1.lookupBuildingRequest
import io.grpc.ManagedChannel

/** Reads acme.routing.v1.RouteCalculationService/QuoteRoute and hands what it keeps to the sink of its kind. */
suspend fun mirrorRouteId(channel: ManagedChannel) {
    val source = RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub(channel)
    val found = RpcBoundary.read { source.quoteRoute(quoteRouteRequest { this.reason = "live-mirror-route-id" }) }
    val row = MirrorRouteIdRecord(
        routeId = found.route.routeId,
        status = found.result.status,
        currencyCode = found.breakdown.currencyCode,
        notes = normalize(found.route.notes),
    )
    for (sink in listOf<MirrorRouteIdDestination>(
        MirrorRouteIdAuditEventValidation(AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel)),
        MirrorRouteIdDriverHomeIntegration(DriverHomeIntegrationServiceGrpcKt.DriverHomeIntegrationServiceCoroutineStub(channel)),
        MirrorRouteIdNotificationValidation(NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub(channel)),
        MirrorRouteIdBuildingQuery(BuildingQueryServiceGrpcKt.BuildingQueryServiceCoroutineStub(channel)),
    )) {
        sink.transmit(row)
    }
}

/** Where mirrorRouteId hands each record. */
interface MirrorRouteIdDestination {
    suspend fun transmit(rec: MirrorRouteIdRecord)
}

/** What mirrorRouteId keeps of each QuoteRoute response. */
data class MirrorRouteIdRecord(val routeId: String, val status: String, val currencyCode: String, val notes: String)

/** Writes records to acme.audit.v1.AuditEventValidationService. */
class MirrorRouteIdAuditEventValidation(private val stub: AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub) : MirrorRouteIdDestination {
    override suspend fun transmit(rec: MirrorRouteIdRecord) {
        val request = previewAuditEventRequest {
            this.auditEvent = auditEvent { this.auditEventId = rec.routeId }
            this.reason = rec.notes
            this.details = PreviewAuditEventRequestKt.details { this.locale = rec.currencyCode }
        }
        RpcBoundary.run("acme.audit.v1.AuditEventValidationService/PreviewAuditEvent") { stub.previewAuditEvent(request) }
    }
}

/** Passes records to acme.driverapp.v1.DriverHomeIntegrationService. */
class MirrorRouteIdDriverHomeIntegration(private val stub: DriverHomeIntegrationServiceGrpcKt.DriverHomeIntegrationServiceCoroutineStub) : MirrorRouteIdDestination {
    override suspend fun transmit(rec: MirrorRouteIdRecord) {
        val request = acknowledgeDriverHomeEventRequest {
            this.driverHomeId = rec.status
            this.details = AcknowledgeDriverHomeEventRequestKt.details { this.endpoint = rec.currencyCode }
        }
        stub.acknowledgeDriverHomeEvent(request)
    }
}

/** Hands records to acme.notifications.v1.NotificationValidationService. */
class MirrorRouteIdNotificationValidation(private val stub: NotificationValidationServiceGrpcKt.NotificationValidationServiceCoroutineStub) : MirrorRouteIdDestination {
    override suspend fun transmit(rec: MirrorRouteIdRecord) {
        stub.getNotificationRules(getNotificationRulesRequest { this.notificationId = rec.currencyCode })
    }
}

/** Sends records to acme.workplaces.v1.BuildingQueryService. */
class MirrorRouteIdBuildingQuery(private val stub: BuildingQueryServiceGrpcKt.BuildingQueryServiceCoroutineStub) : MirrorRouteIdDestination {
    override suspend fun transmit(rec: MirrorRouteIdRecord) {
        stub.lookupBuilding(lookupBuildingRequest { this.buildingId = rec.status })
    }
}
