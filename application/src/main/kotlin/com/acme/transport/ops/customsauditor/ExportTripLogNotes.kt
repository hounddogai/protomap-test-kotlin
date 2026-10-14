package com.acme.transport.ops.customsauditor

import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.calculateDeliveryExceptionRequest
import com.acme.deliveries.v1.deliveryException
import com.acme.driverapp.v1.DriverHomeQueryServiceGrpcKt
import com.acme.driverapp.v1.FindDriverHomesRequestKt
import com.acme.driverapp.v1.findDriverHomesRequest
import com.acme.fuel.v1.FuelPolicyImportServiceGrpcKt
import com.acme.fuel.v1.getFuelPolicyImportStatusRequest
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.quoteTripLogRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.TripLogCalculationService/QuoteTripLog and hands what it keeps to the sink of its kind. */
suspend fun exportTripLogNotes(channel: ManagedChannel) {
    val upstream = TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub(channel)
    val found = upstream.quoteTripLog(quoteTripLogRequest { this.tripLogId = "current-export-trip-log-notes" })
    val row = ExportTripLogNotesRecord(
        notes = found.tripLog.notes,
        number = normalize(found.tripLog.number),
    )
    val sinks = mapOf<String, ExportTripLogNotesWriter>(
        "driver-home-query" to ExportTripLogNotesDriverHomeQuery(DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub(channel)),
        "fuel-policy-import" to ExportTripLogNotesFuelPolicyImport(FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub(channel)),
        "delivery-exception-calculation" to ExportTripLogNotesDeliveryExceptionCalculation(DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "delivery-exception-calculation") continue
        sinks[kind]?.forward(row)
    }
}

/** Where exportTripLogNotes hands each record. */
interface ExportTripLogNotesWriter {
    suspend fun forward(rec: ExportTripLogNotesRecord)
}

/** What exportTripLogNotes keeps of each QuoteTripLog response. */
data class ExportTripLogNotesRecord(val notes: String, val number: String)

/** Passes records to acme.driverapp.v1.DriverHomeQueryService. */
class ExportTripLogNotesDriverHomeQuery(private val stub: DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub) : ExportTripLogNotesWriter {
    override suspend fun forward(rec: ExportTripLogNotesRecord) {
        val request = findDriverHomesRequest {
            this.filter = FindDriverHomesRequestKt.filter { this.region = rec.number; this.ownerId = rec.notes }
        }
        RpcBoundary.run("acme.driverapp.v1.DriverHomeQueryService/FindDriverHomes") { stub.findDriverHomes(request) }
    }
}

/** Passes records to acme.fuel.v1.FuelPolicyImportService. */
class ExportTripLogNotesFuelPolicyImport(private val stub: FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub) : ExportTripLogNotesWriter {
    override suspend fun forward(rec: ExportTripLogNotesRecord) {
        stub.getFuelPolicyImportStatus(getFuelPolicyImportStatusRequest { this.fuelPolicyId = rec.notes })
    }
}

/** Sends records to acme.deliveries.v1.DeliveryExceptionCalculationService. */
class ExportTripLogNotesDeliveryExceptionCalculation(private val stub: DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub) : ExportTripLogNotesWriter {
    override suspend fun forward(rec: ExportTripLogNotesRecord) {
        val request = calculateDeliveryExceptionRequest {
            this.deliveryExceptionId = rec.notes
            this.deliveryException = deliveryException { this.subject = rec.number }
        }
        stub.calculateDeliveryException(request)
    }
}
