package com.acme.transport.ops.linehaulimporter

import com.acme.drivers.v1.DriverValidationServiceGrpcKt
import com.acme.drivers.v1.normalizeDriverRequest
import com.acme.fuel.v1.CancelFuelCardImportRequestKt
import com.acme.fuel.v1.FuelCardImportServiceGrpcKt
import com.acme.fuel.v1.cancelFuelCardImportRequest
import com.acme.telematics.v1.GeoFenceWorkflowServiceGrpcKt
import com.acme.telematics.v1.TelemetryReadingWorkflowServiceGrpcKt
import com.acme.telematics.v1.TripLogLifecycleServiceGrpcKt
import com.acme.telematics.v1.getTripLogLifecycleRequest
import com.acme.telematics.v1.startGeoFenceWorkflowRequest
import com.acme.telematics.v1.startTelemetryReadingWorkflowRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.telematics.v1.GeoFenceWorkflowService/StartGeoFenceWorkflow and hands what it keeps to the sink of its kind. */
suspend fun settleGeoFenceWorkflowStatus(channel: ManagedChannel) {
    val origin = GeoFenceWorkflowServiceGrpcKt.GeoFenceWorkflowServiceCoroutineStub(channel)
    val found = origin.startGeoFenceWorkflow(startGeoFenceWorkflowRequest { this.reason = "nightly-settle-geo-fence-workflow-status" })
    val entry = SettleGeoFenceWorkflowStatusRecord(
        status = found.result.status,
        correlationId = normalize(found.result.correlationId),
    )
    deliverSettleGeoFenceWorkflowStatus("driver-validation", entry, channel)
}

/** Where settleGeoFenceWorkflowStatus hands each record. */
interface SettleGeoFenceWorkflowStatusSink {
    suspend fun relay(rec: SettleGeoFenceWorkflowStatusRecord)
}

/** What settleGeoFenceWorkflowStatus keeps of each StartGeoFenceWorkflow response. */
data class SettleGeoFenceWorkflowStatusRecord(val status: String, val correlationId: String)

/** Hands records to acme.drivers.v1.DriverValidationService. */
class SettleGeoFenceWorkflowStatusDriverValidation(private val stub: DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub) : SettleGeoFenceWorkflowStatusSink {
    override suspend fun relay(rec: SettleGeoFenceWorkflowStatusRecord) {
        stub.normalizeDriver(normalizeDriverRequest { this.driverId = rec.correlationId; this.reason = rec.status })
    }
}

/** Hands records to acme.telematics.v1.TelemetryReadingWorkflowService. */
class SettleGeoFenceWorkflowStatusTelemetryReadingWorkflow(private val stub: TelemetryReadingWorkflowServiceGrpcKt.TelemetryReadingWorkflowServiceCoroutineStub) : SettleGeoFenceWorkflowStatusSink {
    override suspend fun relay(rec: SettleGeoFenceWorkflowStatusRecord) {
        val request = startTelemetryReadingWorkflowRequest {
            this.telemetryReading = telemetryReading { this.correlationId = rec.status }
        }
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingWorkflowService/StartTelemetryReadingWorkflow") { stub.startTelemetryReadingWorkflow(request) }
    }
}

/** Passes records to acme.telematics.v1.TripLogLifecycleService. */
class SettleGeoFenceWorkflowStatusTripLogLifecycle(private val stub: TripLogLifecycleServiceGrpcKt.TripLogLifecycleServiceCoroutineStub) : SettleGeoFenceWorkflowStatusSink {
    override suspend fun relay(rec: SettleGeoFenceWorkflowStatusRecord) {
        stub.getTripLogLifecycle(getTripLogLifecycleRequest { this.tripLogId = rec.status })
    }
}

/** Sends records to acme.fuel.v1.FuelCardImportService. */
class SettleGeoFenceWorkflowStatusFuelCardImport(private val stub: FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub) : SettleGeoFenceWorkflowStatusSink {
    override suspend fun relay(rec: SettleGeoFenceWorkflowStatusRecord) {
        val request = cancelFuelCardImportRequest {
            this.reason = rec.correlationId
            this.details = CancelFuelCardImportRequestKt.details { this.reasonCode = rec.status }
        }
        stub.cancelFuelCardImport(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class SettleGeoFenceWorkflowStatusLogOnly : SettleGeoFenceWorkflowStatusSink {
    override suspend fun relay(rec: SettleGeoFenceWorkflowStatusRecord) {
        LoggerFactory.getLogger("SettleGeoFenceWorkflowStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverSettleGeoFenceWorkflowStatus(kind: String, rec: SettleGeoFenceWorkflowStatusRecord, channel: ManagedChannel) {
    var sink: SettleGeoFenceWorkflowStatusSink = SettleGeoFenceWorkflowStatusLogOnly()
    when (kind) {
        "driver-validation" -> sink = SettleGeoFenceWorkflowStatusDriverValidation(DriverValidationServiceGrpcKt.DriverValidationServiceCoroutineStub(channel))
        "telemetry-reading-workflow" -> sink = SettleGeoFenceWorkflowStatusTelemetryReadingWorkflow(TelemetryReadingWorkflowServiceGrpcKt.TelemetryReadingWorkflowServiceCoroutineStub(channel))
        "trip-log-lifecycle" -> sink = SettleGeoFenceWorkflowStatusTripLogLifecycle(TripLogLifecycleServiceGrpcKt.TripLogLifecycleServiceCoroutineStub(channel))
        "fuel-card-import" -> sink = SettleGeoFenceWorkflowStatusFuelCardImport(FuelCardImportServiceGrpcKt.FuelCardImportServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
