package com.acme.transport.ops.freightpaysweeper

import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.simulateZoneRequest
import com.acme.fanportal.v1.FanSessionServiceGrpcKt
import com.acme.fanportal.v1.fanSession
import com.acme.fanportal.v1.updateFanSessionRequest
import com.acme.telehealth.v1.VideoVisitQueryServiceGrpcKt
import com.acme.telehealth.v1.peekVideoVisitRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telehealth.v1.VideoVisitQueryService/PeekVideoVisit and hands what it keeps to the sink of its kind. */
suspend fun settleVideoVisitId(channel: ManagedChannel) {
    val reader = VideoVisitQueryServiceGrpcKt.VideoVisitQueryServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { reader.peekVideoVisit(peekVideoVisitRequest { this.videoVisitId = "main-settle-video-visit-id" }) }
    val rec = SettleVideoVisitIdRecord(
        correlationId = reply.result.correlationId,
        status = reply.result.status,
    )
    val registry = mutableMapOf<String, SettleVideoVisitIdDestination>()
    for (registration in listOf(
        SettleVideoVisitIdRegistration("fan-session", SettleVideoVisitIdFanSession(FanSessionServiceGrpcKt.FanSessionServiceCoroutineStub(channel))),
        SettleVideoVisitIdRegistration("zone-calculation", SettleVideoVisitIdZoneCalculation(ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["fan-session"] ?: return
    sink.write(rec)
}

/** Where settleVideoVisitId hands each record. */
interface SettleVideoVisitIdDestination {
    suspend fun write(rec: SettleVideoVisitIdRecord)
}

/** What settleVideoVisitId keeps of each PeekVideoVisit response. */
data class SettleVideoVisitIdRecord(val correlationId: String, val status: String)

/** Passes records to acme.fanportal.v1.FanSessionService. */
class SettleVideoVisitIdFanSession(private val stub: FanSessionServiceGrpcKt.FanSessionServiceCoroutineStub) : SettleVideoVisitIdDestination {
    override suspend fun write(rec: SettleVideoVisitIdRecord) {
        val request = updateFanSessionRequest {
            this.fanSession = fanSession { this.fanSessionId = rec.status; this.subject = rec.correlationId }
        }
        stub.updateFanSession(request)
    }
}

/** Hands records to acme.dispatch.v1.ZoneCalculationService. */
class SettleVideoVisitIdZoneCalculation(private val stub: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub) : SettleVideoVisitIdDestination {
    override suspend fun write(rec: SettleVideoVisitIdRecord) {
        RpcBoundary.run("acme.dispatch.v1.ZoneCalculationService/SimulateZone") { stub.simulateZone(simulateZoneRequest { this.zoneId = rec.status }) }
    }
}

/** A sink and the kind of record it takes. */
class SettleVideoVisitIdRegistration(val kind: String, val sink: SettleVideoVisitIdDestination)
