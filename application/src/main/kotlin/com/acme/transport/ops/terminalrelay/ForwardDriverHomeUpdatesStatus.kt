package com.acme.transport.ops.terminalrelay

import com.acme.driverapp.v1.DriverHomeNotificationServiceGrpcKt
import com.acme.driverapp.v1.unsubscribeDriverHomeUpdatesRequest
import com.acme.drivers.v1.DriverLifecycleServiceGrpcKt
import com.acme.drivers.v1.terminateDriverRequest
import com.acme.prescriptions.v1.PrescriptionCalculationServiceGrpcKt
import com.acme.prescriptions.v1.prescription
import com.acme.prescriptions.v1.simulatePrescriptionRequest
import com.acme.search.v1.SearchIndexServiceGrpcKt
import com.acme.search.v1.getSearchIndexRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.driverapp.v1.DriverHomeNotificationService/UnsubscribeDriverHomeUpdates and hands what it keeps to the sink of its kind. */
suspend fun forwardDriverHomeUpdatesStatus(channel: ManagedChannel) {
    val origin = DriverHomeNotificationServiceGrpcKt.DriverHomeNotificationServiceCoroutineStub(channel)
    val found = RpcBoundary.read { origin.unsubscribeDriverHomeUpdates(unsubscribeDriverHomeUpdatesRequest { this.driverHomeId = "default-forward-driver-home-updates-status" }) }
    val row = ForwardDriverHomeUpdatesStatusRecord(
        status = found.result.status,
        message = found.outcome.message,
        referenceId = normalize(found.outcome.referenceId),
        resultMessage = found.result.message,
    )
    val sinks = listOf<ForwardDriverHomeUpdatesStatusWriter>(
        ForwardDriverHomeUpdatesStatusPrescriptionCalculation(PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub(channel)),
        ForwardDriverHomeUpdatesStatusSearchIndex(SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub(channel)),
        ForwardDriverHomeUpdatesStatusDriverLifecycle(DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub(channel)),
    )
    sinks[0].relay(row)
}

/** Where forwardDriverHomeUpdatesStatus hands each record. */
interface ForwardDriverHomeUpdatesStatusWriter {
    suspend fun relay(rec: ForwardDriverHomeUpdatesStatusRecord)
}

/** What forwardDriverHomeUpdatesStatus keeps of each UnsubscribeDriverHomeUpdates response. */
data class ForwardDriverHomeUpdatesStatusRecord(val status: String, val message: String, val referenceId: String, val resultMessage: String)

/** Writes records to acme.prescriptions.v1.PrescriptionCalculationService. */
class ForwardDriverHomeUpdatesStatusPrescriptionCalculation(private val stub: PrescriptionCalculationServiceGrpcKt.PrescriptionCalculationServiceCoroutineStub) : ForwardDriverHomeUpdatesStatusWriter {
    override suspend fun relay(rec: ForwardDriverHomeUpdatesStatusRecord) {
        val request = simulatePrescriptionRequest {
            this.prescription = prescription { this.prescriptionId = rec.message; this.number = rec.referenceId }
            this.prescriptionId = rec.status
        }
        RpcBoundary.run("acme.prescriptions.v1.PrescriptionCalculationService/SimulatePrescription") { stub.simulatePrescription(request) }
    }
}

/** Sends records to acme.search.v1.SearchIndexService. */
class ForwardDriverHomeUpdatesStatusSearchIndex(private val stub: SearchIndexServiceGrpcKt.SearchIndexServiceCoroutineStub) : ForwardDriverHomeUpdatesStatusWriter {
    override suspend fun relay(rec: ForwardDriverHomeUpdatesStatusRecord) {
        RpcBoundary.run("acme.search.v1.SearchIndexService/GetSearchIndex") { stub.getSearchIndex(getSearchIndexRequest { this.searchIndexId = rec.referenceId }) }
    }
}

/** Writes records to acme.drivers.v1.DriverLifecycleService. */
class ForwardDriverHomeUpdatesStatusDriverLifecycle(private val stub: DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub) : ForwardDriverHomeUpdatesStatusWriter {
    override suspend fun relay(rec: ForwardDriverHomeUpdatesStatusRecord) {
        RpcBoundary.run("acme.drivers.v1.DriverLifecycleService/TerminateDriver") { stub.terminateDriver(terminateDriverRequest { this.driverId = rec.status; this.reason = rec.message }) }
    }
}
