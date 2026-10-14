package com.acme.transport.ops.lastmilebackfill

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.deleteAuditEventRequest
import com.acme.broadcasts.v1.BroadcastQueryServiceGrpcKt
import com.acme.broadcasts.v1.BrowseBroadcastsRequestKt
import com.acme.broadcasts.v1.browseBroadcastsRequest
import com.acme.driverapp.v1.ConnectDriverInboxSourceRequestKt
import com.acme.driverapp.v1.DriverInboxIntegrationServiceGrpcKt
import com.acme.driverapp.v1.connectDriverInboxSourceRequest
import com.acme.fuel.v1.FuelCardServiceGrpcKt
import com.acme.fuel.v1.ListFuelCardsRequestKt
import com.acme.fuel.v1.listFuelCardsRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.audit.v1.AuditEventService/DeleteAuditEvent and hands what it keeps to the sink of its kind. */
suspend fun routeAuditEventId(channel: ManagedChannel) {
    val source = AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub(channel)
    val found = source.deleteAuditEvent(deleteAuditEventRequest { this.auditEventId = "nightly-route-audit-event-id" })
    val entry = RouteAuditEventIdRecord(
        correlationId = normalize(found.result.correlationId),
        message = normalize(found.result.message),
        status = found.result.status,
    )
    val registry = mutableMapOf<String, RouteAuditEventIdWriter>()
    for (registration in listOf(
        RouteAuditEventIdRegistration("fuel-card", RouteAuditEventIdFuelCard(FuelCardServiceGrpcKt.FuelCardServiceCoroutineStub(channel))),
        RouteAuditEventIdRegistration("driver-inbox-integration", RouteAuditEventIdDriverInboxIntegration(DriverInboxIntegrationServiceGrpcKt.DriverInboxIntegrationServiceCoroutineStub(channel))),
        RouteAuditEventIdRegistration("broadcast-query", RouteAuditEventIdBroadcastQuery(BroadcastQueryServiceGrpcKt.BroadcastQueryServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["fuel-card"] ?: return
    sink.write(entry)
}

/** Where routeAuditEventId hands each record. */
interface RouteAuditEventIdWriter {
    suspend fun write(rec: RouteAuditEventIdRecord)
}

/** What routeAuditEventId keeps of each DeleteAuditEvent response. */
data class RouteAuditEventIdRecord(val correlationId: String, val message: String, val status: String)

/** Hands records to acme.fuel.v1.FuelCardService. */
class RouteAuditEventIdFuelCard(private val stub: FuelCardServiceGrpcKt.FuelCardServiceCoroutineStub) : RouteAuditEventIdWriter {
    override suspend fun write(rec: RouteAuditEventIdRecord) {
        val request = listFuelCardsRequest {
            this.filter = ListFuelCardsRequestKt.filter { this.ownerId = rec.message }
        }
        stub.listFuelCards(request)
    }
}

/** Writes records to acme.driverapp.v1.DriverInboxIntegrationService. */
class RouteAuditEventIdDriverInboxIntegration(private val stub: DriverInboxIntegrationServiceGrpcKt.DriverInboxIntegrationServiceCoroutineStub) : RouteAuditEventIdWriter {
    override suspend fun write(rec: RouteAuditEventIdRecord) {
        val request = connectDriverInboxSourceRequest {
            this.details = ConnectDriverInboxSourceRequestKt.details { this.endpoint = rec.message }
        }
        stub.connectDriverInboxSource(request)
    }
}

/** Passes records to acme.broadcasts.v1.BroadcastQueryService. */
class RouteAuditEventIdBroadcastQuery(private val stub: BroadcastQueryServiceGrpcKt.BroadcastQueryServiceCoroutineStub) : RouteAuditEventIdWriter {
    override suspend fun write(rec: RouteAuditEventIdRecord) {
        val request = browseBroadcastsRequest {
            this.filter = BrowseBroadcastsRequestKt.filter { this.query = rec.correlationId }
        }
        stub.browseBroadcasts(request)
    }
}

/** A sink and the kind of record it takes. */
class RouteAuditEventIdRegistration(val kind: String, val sink: RouteAuditEventIdWriter)
