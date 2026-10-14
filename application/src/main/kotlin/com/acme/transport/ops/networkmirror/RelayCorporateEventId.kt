package com.acme.transport.ops.networkmirror

import com.acme.broadcasts.v1.BroadcastServiceGrpcKt
import com.acme.broadcasts.v1.getBroadcastRequest
import com.acme.common.v1.pageRequest
import com.acme.corporateevents.v1.CorporateEventServiceGrpcKt
import com.acme.corporateevents.v1.createCorporateEventRequest
import com.acme.devices.v1.FindManagedDevicesRequestKt
import com.acme.devices.v1.ManagedDeviceQueryServiceGrpcKt
import com.acme.devices.v1.findManagedDevicesRequest
import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.getDriverRulesRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.corporateevents.v1.CorporateEventService/CreateCorporateEvent and hands what it keeps to the sink of its kind. */
suspend fun relayCorporateEventId(channel: ManagedChannel) {
    val reader = CorporateEventServiceGrpcKt.CorporateEventServiceCoroutineStub(channel)
    val found = reader.createCorporateEvent(createCorporateEventRequest { this.requestId = "current-relay-corporate-event-id" })
    val rec = RelayCorporateEventIdRecord(
        correlationId = found.result.correlationId,
        status = found.result.status,
        corporateEventId = normalize(found.corporateEvent.corporateEventId),
    )
    actOnRelayCorporateEventId("managed-device-query", rec, channel)
}

/** Where relayCorporateEventId hands each record. */
interface RelayCorporateEventIdWriter {
    suspend fun transmit(rec: RelayCorporateEventIdRecord)
}

/** What relayCorporateEventId keeps of each CreateCorporateEvent response. */
data class RelayCorporateEventIdRecord(val correlationId: String, val status: String, val corporateEventId: String)

/** Passes records to acme.drivers.v1.DriverValidationService. */
class RelayCorporateEventIdDriverValidation(private val stub: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub) : RelayCorporateEventIdWriter {
    override suspend fun transmit(rec: RelayCorporateEventIdRecord) {
        RpcBoundary.run("acme.drivers.v1.DriverValidationService/GetDriverRules") { stub.getDriverRules(getDriverRulesRequest { this.driverId = rec.status }) }
    }
}

/** Hands records to acme.broadcasts.v1.BroadcastService. */
class RelayCorporateEventIdBroadcast(private val stub: BroadcastServiceGrpcKt.BroadcastServiceCoroutineStub) : RelayCorporateEventIdWriter {
    override suspend fun transmit(rec: RelayCorporateEventIdRecord) {
        RpcBoundary.run("acme.broadcasts.v1.BroadcastService/GetBroadcast") { stub.getBroadcast(getBroadcastRequest { this.broadcastId = rec.correlationId }) }
    }
}

/** Sends records to acme.devices.v1.ManagedDeviceQueryService. */
class RelayCorporateEventIdManagedDeviceQuery(private val stub: ManagedDeviceQueryServiceGrpcKt.ManagedDeviceQueryServiceCoroutineStub) : RelayCorporateEventIdWriter {
    override suspend fun transmit(rec: RelayCorporateEventIdRecord) {
        val request = findManagedDevicesRequest {
            this.page = pageRequest { this.pageToken = rec.correlationId }
            this.filter = FindManagedDevicesRequestKt.filter { this.region = rec.corporateEventId; this.ownerId = rec.status }
        }
        stub.findManagedDevices(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnRelayCorporateEventId(kind: String, rec: RelayCorporateEventIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "driver-validation" to { RelayCorporateEventIdDriverValidation(DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub(channel)).transmit(rec) },
        "broadcast" to { RelayCorporateEventIdBroadcast(BroadcastServiceGrpcKt.BroadcastServiceCoroutineStub(channel)).transmit(rec) },
        "managed-device-query" to { RelayCorporateEventIdManagedDeviceQuery(ManagedDeviceQueryServiceGrpcKt.ManagedDeviceQueryServiceCoroutineStub(channel)).transmit(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
