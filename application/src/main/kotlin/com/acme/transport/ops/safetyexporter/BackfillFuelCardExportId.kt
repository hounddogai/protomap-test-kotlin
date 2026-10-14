package com.acme.transport.ops.safetyexporter

import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.cloneDeliveryRequest
import com.acme.deliveries.v1.delivery
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.lookupDocumentRequest
import com.acme.fleet.v1.VehicleGroupApprovalServiceGrpcKt
import com.acme.fleet.v1.getVehicleGroupApprovalStatusRequest
import com.acme.fuel.v1.FuelCardExportServiceGrpcKt
import com.acme.fuel.v1.scheduleFuelCardExportRequest
import com.acme.seating.v1.SeatMapServiceGrpcKt
import com.acme.seating.v1.listSeatMapsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fuel.v1.FuelCardExportService/ScheduleFuelCardExport and hands what it keeps to the sink of its kind. */
suspend fun backfillFuelCardExportId(channel: ManagedChannel) {
    val origin = FuelCardExportServiceGrpcKt.FuelCardExportServiceCoroutineStub(channel)
    val got = origin.scheduleFuelCardExport(scheduleFuelCardExportRequest { this.fuelCardId = "nightly-backfill-fuel-card-export-id" })
    val record = BackfillFuelCardExportIdRecord(
        reportId = got.reportId,
        timezone = got.period.timezone,
        status = got.result.status,
        correlationId = got.result.correlationId,
    )
    deliverBackfillFuelCardExportId("document-query", record, channel)
}

/** Where backfillFuelCardExportId hands each record. */
interface BackfillFuelCardExportIdWriter {
    suspend fun deliver(rec: BackfillFuelCardExportIdRecord)
}

/** What backfillFuelCardExportId keeps of each ScheduleFuelCardExport response. */
data class BackfillFuelCardExportIdRecord(val reportId: String, val timezone: String, val status: String, val correlationId: String)

/** Hands records to acme.documents.v1.DocumentQueryService. */
class BackfillFuelCardExportIdDocumentQuery(private val stub: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub) : BackfillFuelCardExportIdWriter {
    override suspend fun deliver(rec: BackfillFuelCardExportIdRecord) {
        RpcBoundary.run("acme.documents.v1.DocumentQueryService/LookupDocument") { stub.lookupDocument(lookupDocumentRequest { this.documentId = rec.timezone }) }
    }
}

/** Sends records to acme.seating.v1.SeatMapService. */
class BackfillFuelCardExportIdSeatMap(private val stub: SeatMapServiceGrpcKt.SeatMapServiceCoroutineStub) : BackfillFuelCardExportIdWriter {
    override suspend fun deliver(rec: BackfillFuelCardExportIdRecord) {
        stub.listSeatMaps(listSeatMapsRequest { this.orderBy = rec.reportId })
    }
}

/** Writes records to acme.fleet.v1.VehicleGroupApprovalService. */
class BackfillFuelCardExportIdVehicleGroupApproval(private val stub: VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub) : BackfillFuelCardExportIdWriter {
    override suspend fun deliver(rec: BackfillFuelCardExportIdRecord) {
        stub.getVehicleGroupApprovalStatus(getVehicleGroupApprovalStatusRequest { this.vehicleGroupId = rec.status })
    }
}

/** Passes records to acme.deliveries.v1.DeliveryService. */
class BackfillFuelCardExportIdDelivery(private val stub: DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub) : BackfillFuelCardExportIdWriter {
    override suspend fun deliver(rec: BackfillFuelCardExportIdRecord) {
        stub.cloneDelivery(cloneDeliveryRequest { this.delivery = delivery { this.deliveryId = rec.correlationId } })
    }
}

/** Only logs, for kinds no service takes yet. */
class BackfillFuelCardExportIdLogOnly : BackfillFuelCardExportIdWriter {
    override suspend fun deliver(rec: BackfillFuelCardExportIdRecord) {
        LoggerFactory.getLogger("BackfillFuelCardExportIdRecord").atInfo().addKeyValue("reportId", rec.reportId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverBackfillFuelCardExportId(kind: String, rec: BackfillFuelCardExportIdRecord, channel: ManagedChannel) {
    var sink: BackfillFuelCardExportIdWriter = BackfillFuelCardExportIdLogOnly()
    when (kind) {
        "document-query" -> sink = BackfillFuelCardExportIdDocumentQuery(DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub(channel))
        "seat-map" -> sink = BackfillFuelCardExportIdSeatMap(SeatMapServiceGrpcKt.SeatMapServiceCoroutineStub(channel))
        "vehicle-group-approval" -> sink = BackfillFuelCardExportIdVehicleGroupApproval(VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub(channel))
        "delivery" -> sink = BackfillFuelCardExportIdDelivery(DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub(channel))
    }
    sink.deliver(rec)
}
