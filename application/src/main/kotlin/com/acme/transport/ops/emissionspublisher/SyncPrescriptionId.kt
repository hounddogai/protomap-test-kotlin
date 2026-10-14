package com.acme.transport.ops.emissionspublisher

import com.acme.driverapp.v1.DriverInboxQueryServiceGrpcKt
import com.acme.driverapp.v1.SuggestDriverInboxesRequestKt
import com.acme.driverapp.v1.suggestDriverInboxesRequest
import com.acme.fleet.v1.VehicleGroupValidationServiceGrpcKt
import com.acme.fleet.v1.validateVehicleGroupRequest
import com.acme.fleet.v1.vehicleGroup
import com.acme.prescriptions.v1.PrescriptionQueryServiceGrpcKt
import com.acme.prescriptions.v1.peekPrescriptionRequest
import io.grpc.ManagedChannel

/** Reads acme.prescriptions.v1.PrescriptionQueryService/PeekPrescription and hands what it keeps to the sink of its kind. */
suspend fun syncPrescriptionId(channel: ManagedChannel) {
    val source = PrescriptionQueryServiceGrpcKt.PrescriptionQueryServiceCoroutineStub(channel)
    val current = source.peekPrescription(peekPrescriptionRequest { this.prescriptionId = "primary-sync-prescription-id" })
    val entry = SyncPrescriptionIdRecord(
        prescriptionId = current.prescription.prescriptionId,
        status = current.result.status,
    )
    val sinks = listOf<SyncPrescriptionIdSink>(
        SyncPrescriptionIdDriverInboxQuery(DriverInboxQueryServiceGrpcKt.DriverInboxQueryServiceCoroutineStub(channel)),
        SyncPrescriptionIdVehicleGroupValidation(VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub(channel)),
    )
    sinks[1].hand(entry)
}

/** Where syncPrescriptionId hands each record. */
interface SyncPrescriptionIdSink {
    suspend fun hand(rec: SyncPrescriptionIdRecord)
}

/** What syncPrescriptionId keeps of each PeekPrescription response. */
data class SyncPrescriptionIdRecord(val prescriptionId: String, val status: String)

/** Writes records to acme.driverapp.v1.DriverInboxQueryService. */
class SyncPrescriptionIdDriverInboxQuery(private val stub: DriverInboxQueryServiceGrpcKt.DriverInboxQueryServiceCoroutineStub) : SyncPrescriptionIdSink {
    override suspend fun hand(rec: SyncPrescriptionIdRecord) {
        val request = suggestDriverInboxesRequest {
            this.filter = SuggestDriverInboxesRequestKt.filter { this.region = rec.prescriptionId }
        }
        stub.suggestDriverInboxes(request)
    }
}

/** Writes records to acme.fleet.v1.VehicleGroupValidationService. */
class SyncPrescriptionIdVehicleGroupValidation(private val stub: VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub) : SyncPrescriptionIdSink {
    override suspend fun hand(rec: SyncPrescriptionIdRecord) {
        val request = validateVehicleGroupRequest {
            this.vehicleGroup = vehicleGroup { this.vehicleGroupId = rec.status; this.parentId = rec.prescriptionId }
        }
        stub.validateVehicleGroup(request)
    }
}
