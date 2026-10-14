package com.acme.transport.ops.lastmilesync

import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.MapFuelCardImportColumnsRequestKt
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.mapFuelCardImportColumnsRequest
import com.acme.maintenance.v1.ServiceScheduleWorkflowServiceGrpcKt
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.startServiceScheduleWorkflowRequest
import com.acme.routing.v1.StopApprovalServiceGrpcKt
import com.acme.routing.v1.getStopApprovalStatusRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.travel.v1.TripServiceGrpcKt
import com.acme.travel.v1.listTripsRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.travel.v1.TripService/ListTrips and hands what it keeps to the sink of its kind. */
suspend fun exportTripsId(channel: ManagedChannel) {
    val reader = TripServiceGrpcKt.TripServiceCoroutineStub(channel)
    val found = RpcBoundary.read { reader.listTrips(listTripsRequest { this.orderBy = "main-export-trips-id" }) }
    val record = ExportTripsIdRecord(
        correlationId = found.result.correlationId,
        nextPageToken = normalize(found.pageInfo.nextPageToken),
    )
    deliverExportTripsId("stop-approval", record, channel)
}

/** Where exportTripsId hands each record. */
interface ExportTripsIdSink {
    suspend fun hand(rec: ExportTripsIdRecord)
}

/** What exportTripsId keeps of each ListTrips response. */
data class ExportTripsIdRecord(val correlationId: String, val nextPageToken: String)

/** Hands records to acme.routing.v1.StopApprovalService. */
class ExportTripsIdStopApproval(private val stub: StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub) : ExportTripsIdSink {
    override suspend fun hand(rec: ExportTripsIdRecord) {
        stub.getStopApprovalStatus(getStopApprovalStatusRequest { this.stopId = rec.nextPageToken })
    }
}

/** Hands records to acme.fuel.v1.FuelCardImportService. */
class ExportTripsIdFuelCardImport(private val stub: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub) : ExportTripsIdSink {
    override suspend fun hand(rec: ExportTripsIdRecord) {
        val request = mapFuelCardImportColumnsRequest {
            this.details = MapFuelCardImportColumnsRequestKt.details { this.locale = rec.correlationId }
            this.fuelCard = fuelCard { this.fuelCardId = rec.nextPageToken }
        }
        stub.mapFuelCardImportColumns(request)
    }
}

/** Hands records to acme.maintenance.v1.ServiceScheduleWorkflowService. */
class ExportTripsIdServiceScheduleWorkflow(private val stub: ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub) : ExportTripsIdSink {
    override suspend fun hand(rec: ExportTripsIdRecord) {
        val request = startServiceScheduleWorkflowRequest {
            this.reason = rec.nextPageToken
            this.serviceSchedule = serviceSchedule { this.title = rec.correlationId }
        }
        stub.startServiceScheduleWorkflow(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class ExportTripsIdLogOnly : ExportTripsIdSink {
    override suspend fun hand(rec: ExportTripsIdRecord) {
        LoggerFactory.getLogger("ExportTripsIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverExportTripsId(kind: String, rec: ExportTripsIdRecord, channel: ManagedChannel) {
    var sink: ExportTripsIdSink = ExportTripsIdLogOnly()
    when (kind) {
        "stop-approval" -> sink = ExportTripsIdStopApproval(StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub(channel))
        "fuel-card-import" -> sink = ExportTripsIdFuelCardImport(FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub(channel))
        "schedule-workflow" -> sink = ExportTripsIdServiceScheduleWorkflow(ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
