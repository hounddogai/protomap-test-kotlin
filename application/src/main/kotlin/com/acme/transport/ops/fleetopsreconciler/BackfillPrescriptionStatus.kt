package com.acme.transport.ops.fleetopsreconciler

import com.acme.prescriptions.v1.PrescriptionCalculationServiceGrpcKt
import com.acme.prescriptions.v1.quotePrescriptionRequest
import com.acme.telematics.v1.GeoFenceSyncServiceGrpcKt
import com.acme.telematics.v1.ResolveGeoFenceConflictRequestKt
import com.acme.telematics.v1.SyncTelemetryReadingsRequestKt
import com.acme.telematics.v1.TelemetryReadingSyncServiceGrpcKt
import com.acme.telematics.v1.resolveGeoFenceConflictRequest
import com.acme.telematics.v1.syncTelemetryReadingsRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.SignalYardMoveWorkflowRequestKt
import com.acme.yards.v1.YardMoveWorkflowServiceGrpcKt
import com.acme.yards.v1.signalYardMoveWorkflowRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.prescriptions.v1.PrescriptionCalculationService/QuotePrescription and hands what it keeps to the sink of its kind. */
suspend fun backfillPrescriptionStatus(channel: ManagedChannel) {
    val origin = PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub(channel)
    val current = RpcBoundary.read { origin.quotePrescription(quotePrescriptionRequest { this.reason = "nightly-backfill-prescription-status" }) }
    val entry = BackfillPrescriptionStatusRecord(
        status = current.result.status,
        number = current.prescription.number,
        correlationId = current.result.correlationId,
        message = current.result.message,
    )
    deliverBackfillPrescriptionStatus("yard-move-workflow", entry, channel)
}

/** Where backfillPrescriptionStatus hands each record. */
interface BackfillPrescriptionStatusDestination {
    suspend fun transmit(rec: BackfillPrescriptionStatusRecord)
}

/** What backfillPrescriptionStatus keeps of each QuotePrescription response. */
data class BackfillPrescriptionStatusRecord(val status: String, val number: String, val correlationId: String, val message: String)

/** Passes records to acme.telematics.v1.GeoFenceSyncService. */
class BackfillPrescriptionStatusGeoFenceSync(private val stub: GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub) : BackfillPrescriptionStatusDestination {
    override suspend fun transmit(rec: BackfillPrescriptionStatusRecord) {
        val request = resolveGeoFenceConflictRequest {
            this.details = ResolveGeoFenceConflictRequestKt.details { this.resolution = rec.correlationId }
            this.geoFenceId = rec.number
            this.reason = rec.message
        }
        RpcBoundary.run("acme.telematics.v1.GeoFenceSyncService/ResolveGeoFenceConflict") { stub.resolveGeoFenceConflict(request) }
    }
}

/** Writes records to acme.yards.v1.YardMoveWorkflowService. */
class BackfillPrescriptionStatusYardMoveWorkflow(private val stub: YardMoveWorkflowServiceGrpcKt.YardMoveWorkflowServiceCoroutineStub) : BackfillPrescriptionStatusDestination {
    override suspend fun transmit(rec: BackfillPrescriptionStatusRecord) {
        val request = signalYardMoveWorkflowRequest {
            this.details = SignalYardMoveWorkflowRequestKt.details { this.initiatedBy = rec.correlationId; this.step = rec.status }
            this.yardMoveId = rec.message
        }
        RpcBoundary.run("acme.yards.v1.YardMoveWorkflowService/SignalYardMoveWorkflow") { stub.signalYardMoveWorkflow(request) }
    }
}

/** Passes records to acme.telematics.v1.TelemetryReadingSyncService. */
class BackfillPrescriptionStatusTelemetryReadingSync(private val stub: TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub) : BackfillPrescriptionStatusDestination {
    override suspend fun transmit(rec: BackfillPrescriptionStatusRecord) {
        val request = syncTelemetryReadingsRequest {
            this.reason = rec.message
            this.details = SyncTelemetryReadingsRequestKt.details { this.checksum = rec.correlationId }
        }
        stub.syncTelemetryReadings(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class BackfillPrescriptionStatusLogOnly : BackfillPrescriptionStatusDestination {
    override suspend fun transmit(rec: BackfillPrescriptionStatusRecord) {
        LoggerFactory.getLogger("BackfillPrescriptionStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverBackfillPrescriptionStatus(kind: String, rec: BackfillPrescriptionStatusRecord, channel: ManagedChannel) {
    var sink: BackfillPrescriptionStatusDestination = BackfillPrescriptionStatusLogOnly()
    when (kind) {
        "geo-fence-sync" -> sink = BackfillPrescriptionStatusGeoFenceSync(GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub(channel))
        "yard-move-workflow" -> sink = BackfillPrescriptionStatusYardMoveWorkflow(YardMoveWorkflowServiceGrpcKt.YardMoveWorkflowServiceCoroutineStub(channel))
        "telemetry-reading-sync" -> sink = BackfillPrescriptionStatusTelemetryReadingSync(TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
