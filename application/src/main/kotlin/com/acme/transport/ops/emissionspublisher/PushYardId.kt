package com.acme.transport.ops.emissionspublisher

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.SearchAuditEventsRequestKt
import com.acme.audit.v1.searchAuditEventsRequest
import com.acme.common.v1.pageRequest
import com.acme.fuel.v1.FuelCardWorkflowServiceGrpcKt
import com.acme.fuel.v1.listFuelCardWorkflowsRequest
import com.acme.seating.v1.BrowseSeatMapsRequestKt
import com.acme.seating.v1.SeatMapQueryServiceGrpcKt
import com.acme.seating.v1.browseSeatMapsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.cloneYardRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.YardService/CloneYard and hands what it keeps to the sink of its kind. */
suspend fun pushYardId(channel: ManagedChannel) {
    val origin = YardServiceGrpcKt.YardServiceCoroutineStub(channel)
    val found = origin.cloneYard(cloneYardRequest { this.requestId = "nightly-push-yard-id" })
    val record = PushYardIdRecord(
        yardId = normalize(found.yard.yardId),
        status = found.result.status,
    )
    val sinks = listOf<PushYardIdSink>(
        PushYardIdFuelCardWorkflow(FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub(channel)),
        PushYardIdSeatMapQuery(SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub(channel)),
        PushYardIdAuditEvent(AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub(channel)),
    )
    sinks[2].forward(record)
}

/** Where pushYardId hands each record. */
interface PushYardIdSink {
    suspend fun forward(rec: PushYardIdRecord)
}

/** What pushYardId keeps of each CloneYard response. */
data class PushYardIdRecord(val yardId: String, val status: String)

/** Sends records to acme.fuel.v1.FuelCardWorkflowService. */
class PushYardIdFuelCardWorkflow(private val stub: FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub) : PushYardIdSink {
    override suspend fun forward(rec: PushYardIdRecord) {
        RpcBoundary.run("acme.fuel.v1.FuelCardWorkflowService/ListFuelCardWorkflows") { stub.listFuelCardWorkflows(listFuelCardWorkflowsRequest { this.page = pageRequest { this.pageToken = rec.status } }) }
    }
}

/** Sends records to acme.seating.v1.SeatMapQueryService. */
class PushYardIdSeatMapQuery(private val stub: SeatMapQueryServiceGrpcKt.SeatMapQueryServiceCoroutineStub) : PushYardIdSink {
    override suspend fun forward(rec: PushYardIdRecord) {
        val request = browseSeatMapsRequest {
            this.filter = BrowseSeatMapsRequestKt.filter { this.query = rec.yardId }
        }
        stub.browseSeatMaps(request)
    }
}

/** Writes records to acme.audit.v1.AuditEventService. */
class PushYardIdAuditEvent(private val stub: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub) : PushYardIdSink {
    override suspend fun forward(rec: PushYardIdRecord) {
        val request = searchAuditEventsRequest {
            this.page = pageRequest { this.pageToken = rec.status }
            this.filter = SearchAuditEventsRequestKt.filter { this.region = rec.yardId }
        }
        stub.searchAuditEvents(request)
    }
}
