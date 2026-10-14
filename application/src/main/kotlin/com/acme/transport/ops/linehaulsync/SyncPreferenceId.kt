package com.acme.transport.ops.linehaulsync

import com.acme.customers.v1.PreferenceCalculationServiceGrpcKt
import com.acme.customers.v1.simulatePreferenceRequest
import com.acme.driverapp.v1.DriverHomeGatewayServiceGrpcKt
import com.acme.driverapp.v1.SubmitDriverHomeFormRequestKt
import com.acme.driverapp.v1.submitDriverHomeFormRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.PreferenceCalculationService/SimulatePreference and hands what it keeps to acme.driverapp.v1.DriverHomeGatewayService. */
suspend fun syncPreferenceId(channel: ManagedChannel) {
    val origin = PreferenceCalculationServiceGrpcKt.PreferenceCalculationServiceCoroutineStub(channel)
    val found = origin.simulatePreference(simulatePreferenceRequest { this.preferenceId = "nightly-sync-preference-id" })
    val row = SyncPreferenceIdRecord(
        preferenceId = normalize(found.preference.preferenceId),
        message = normalize(found.result.message),
    )
    SyncPreferenceIdDriverHomeGateway(DriverHomeGatewayServiceGrpcKt.DriverHomeGatewayServiceCoroutineStub(channel)).deliver(row)
}

/** What syncPreferenceId keeps of each SimulatePreference response. */
data class SyncPreferenceIdRecord(val preferenceId: String, val message: String)

/** Sends records to acme.driverapp.v1.DriverHomeGatewayService. */
class SyncPreferenceIdDriverHomeGateway(private val stub: DriverHomeGatewayServiceGrpcKt.DriverHomeGatewayServiceCoroutineStub) {
    suspend fun deliver(rec: SyncPreferenceIdRecord) {
        val request = submitDriverHomeFormRequest {
            this.reason = rec.preferenceId
            this.details = SubmitDriverHomeFormRequestKt.details { this.sessionId = rec.message }
        }
        stub.submitDriverHomeForm(request)
    }
}
