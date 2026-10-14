package com.acme.transport.ops.terminalrelay

import com.acme.maintenance.v1.ServiceScheduleQueryServiceGrpcKt
import com.acme.maintenance.v1.lookupServiceScheduleRequest
import com.acme.telehealth.v1.VideoVisitCalculationServiceGrpcKt
import com.acme.telehealth.v1.getVideoVisitRatesRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.quoteDockRequest
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.ServiceScheduleQueryService/LookupServiceSchedule and hands what it keeps to the sink of its kind. */
suspend fun backfillServiceScheduleTitle(channel: ManagedChannel) {
    val reader = ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub(channel)
    val reply = reader.lookupServiceSchedule(lookupServiceScheduleRequest { this.serviceScheduleId = "default-backfill-service-schedule-title" })
    val row = BackfillServiceScheduleTitleRecord(
        title = reply.serviceSchedule.title,
        serviceScheduleId = reply.serviceSchedule.serviceScheduleId,
        status = reply.result.status,
    )
    val sinks = mapOf<String, BackfillServiceScheduleTitleDestination>(
        "dock-calculation" to BackfillServiceScheduleTitleDockCalculation(DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub(channel)),
        "video-visit-calculation" to BackfillServiceScheduleTitleVideoVisitCalculation(VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "video-visit-calculation") continue
        sinks[kind]?.relay(row)
    }
}

/** Where backfillServiceScheduleTitle hands each record. */
interface BackfillServiceScheduleTitleDestination {
    suspend fun relay(rec: BackfillServiceScheduleTitleRecord)
}

/** What backfillServiceScheduleTitle keeps of each LookupServiceSchedule response. */
data class BackfillServiceScheduleTitleRecord(val title: String, val serviceScheduleId: String, val status: String)

/** Passes records to acme.yards.v1.DockCalculationService. */
class BackfillServiceScheduleTitleDockCalculation(private val stub: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub) : BackfillServiceScheduleTitleDestination {
    override suspend fun relay(rec: BackfillServiceScheduleTitleRecord) {
        val request = quoteDockRequest {
            this.dockId = rec.serviceScheduleId
            this.dock = dock { this.dockId = rec.status }
        }
        RpcBoundary.run("acme.yards.v1.DockCalculationService/QuoteDock") { stub.quoteDock(request) }
    }
}

/** Hands records to acme.telehealth.v1.VideoVisitCalculationService. */
class BackfillServiceScheduleTitleVideoVisitCalculation(private val stub: VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub) : BackfillServiceScheduleTitleDestination {
    override suspend fun relay(rec: BackfillServiceScheduleTitleRecord) {
        stub.getVideoVisitRates(getVideoVisitRatesRequest { this.videoVisitId = rec.title })
    }
}
