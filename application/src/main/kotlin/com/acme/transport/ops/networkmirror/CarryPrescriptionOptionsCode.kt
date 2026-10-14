package com.acme.transport.ops.networkmirror

import com.acme.common.v1.pageRequest
import com.acme.driverapp.v1.DriverHomeNotificationServiceGrpcKt
import com.acme.driverapp.v1.listDriverHomeSubscriptionsRequest
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.ListFuelTransactionConflictsRequestKt
import com.acme.fuel.v1.listFuelTransactionConflictsRequest
import com.acme.prescriptions.v1.PrescriptionCalculationServiceGrpcKt
import com.acme.prescriptions.v1.comparePrescriptionOptionsRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.venues.v1.VenueServiceGrpcKt
import com.acme.venues.v1.searchVenuesRequest
import io.grpc.ManagedChannel

/** Reads acme.prescriptions.v1.PrescriptionCalculationService/ComparePrescriptionOptions and hands what it keeps to the sink of its kind. */
suspend fun carryPrescriptionOptionsCode(channel: ManagedChannel) {
    val source = PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub(channel)
    val got = source.comparePrescriptionOptions(comparePrescriptionOptionsRequest { this.reason = "main-carry-prescription-options-code" })
    val row = CarryPrescriptionOptionsCodeRecord(
        currencyCode = got.breakdown.currencyCode,
        prescriptionId = got.prescription.prescriptionId,
        number = got.prescription.number,
    )
    actOnCarryPrescriptionOptionsCode("driver-home-notification", row, channel)
}

/** Where carryPrescriptionOptionsCode hands each record. */
interface CarryPrescriptionOptionsCodeDestination {
    suspend fun relay(rec: CarryPrescriptionOptionsCodeRecord)
}

/** What carryPrescriptionOptionsCode keeps of each ComparePrescriptionOptions response. */
data class CarryPrescriptionOptionsCodeRecord(val currencyCode: String, val prescriptionId: String, val number: String)

/** Passes records to acme.fuel.v1.FuelTransactionSyncService. */
class CarryPrescriptionOptionsCodeFuelTransactionSync(private val stub: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub) : CarryPrescriptionOptionsCodeDestination {
    override suspend fun relay(rec: CarryPrescriptionOptionsCodeRecord) {
        val request = listFuelTransactionConflictsRequest {
            this.page = pageRequest { this.pageToken = rec.prescriptionId }
            this.filter = ListFuelTransactionConflictsRequestKt.filter { this.query = rec.number; this.region = rec.currencyCode }
        }
        RpcBoundary.run("acme.fuel.v1.FuelTransactionSyncService/ListFuelTransactionConflicts") { stub.listFuelTransactionConflicts(request) }
    }
}

/** Sends records to acme.driverapp.v1.DriverHomeNotificationService. */
class CarryPrescriptionOptionsCodeDriverHomeNotification(private val stub: DriverHomeNotificationServiceGrpcKt.DriverHomeNotificationServiceCoroutineStub) : CarryPrescriptionOptionsCodeDestination {
    override suspend fun relay(rec: CarryPrescriptionOptionsCodeRecord) {
        val request = listDriverHomeSubscriptionsRequest {
            this.page = pageRequest { this.pageToken = rec.number }
        }
        RpcBoundary.run("acme.driverapp.v1.DriverHomeNotificationService/ListDriverHomeSubscriptions") { stub.listDriverHomeSubscriptions(request) }
    }
}

/** Hands records to acme.venues.v1.VenueService. */
class CarryPrescriptionOptionsCodeVenue(private val stub: VenueServiceGrpcKt.VenueServiceCoroutineStub) : CarryPrescriptionOptionsCodeDestination {
    override suspend fun relay(rec: CarryPrescriptionOptionsCodeRecord) {
        val request = searchVenuesRequest {
            this.orderBy = rec.currencyCode
            this.page = pageRequest { this.pageToken = rec.prescriptionId }
        }
        stub.searchVenues(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnCarryPrescriptionOptionsCode(kind: String, rec: CarryPrescriptionOptionsCodeRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "fuel-transaction-sync" to { CarryPrescriptionOptionsCodeFuelTransactionSync(FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub(channel)).relay(rec) },
        "driver-home-notification" to { CarryPrescriptionOptionsCodeDriverHomeNotification(DriverHomeNotificationServiceGrpcKt.DriverHomeNotificationServiceCoroutineStub(channel)).relay(rec) },
        "venue" to { CarryPrescriptionOptionsCodeVenue(VenueServiceGrpcKt.VenueServiceCoroutineStub(channel)).relay(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
