package com.acme.transport.ops.lastmilebackfill

import com.acme.customers.v1.PreferenceImportServiceGrpcKt
import com.acme.customers.v1.mapPreferenceImportColumnsRequest
import com.acme.customers.v1.preference
import com.acme.keymanagement.v1.CryptoKeyServiceGrpcKt
import com.acme.keymanagement.v1.getCryptoKeyRequest
import com.acme.maintenance.v1.ServiceScheduleValidationServiceGrpcKt
import com.acme.maintenance.v1.validateServiceScheduleRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.ServiceScheduleValidationService/ValidateServiceSchedule and hands what it keeps to the sink of its kind. */
suspend fun relayServiceScheduleId(channel: ManagedChannel) {
    val source = ServiceScheduleValidationServiceGrpcKt.ServiceScheduleValidationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { source.validateServiceSchedule(validateServiceScheduleRequest { this.serviceScheduleId = "current-relay-service-schedule-id" }) }
    val record = RelayServiceScheduleIdRecord(
        serviceScheduleId = normalize(got.serviceSchedule.serviceScheduleId),
        status = got.result.status,
        referenceId = got.outcome.referenceId,
    )
    val sinks = listOf<RelayServiceScheduleIdTarget>(
        RelayServiceScheduleIdPreferenceImport(PreferenceImportServiceGrpcKt.PreferenceImportServiceCoroutineStub(channel)),
        RelayServiceScheduleIdCryptoKey(CryptoKeyServiceGrpcKt.CryptoKeyServiceCoroutineStub(channel)),
    )
    sinks[0].forward(record)
}

/** Where relayServiceScheduleId hands each record. */
interface RelayServiceScheduleIdTarget {
    suspend fun forward(rec: RelayServiceScheduleIdRecord)
}

/** What relayServiceScheduleId keeps of each ValidateServiceSchedule response. */
data class RelayServiceScheduleIdRecord(val serviceScheduleId: String, val status: String, val referenceId: String)

/** Hands records to acme.customers.v1.PreferenceImportService. */
class RelayServiceScheduleIdPreferenceImport(private val stub: PreferenceImportServiceGrpcKt.PreferenceImportServiceCoroutineStub) : RelayServiceScheduleIdTarget {
    override suspend fun forward(rec: RelayServiceScheduleIdRecord) {
        val request = mapPreferenceImportColumnsRequest {
            this.preference = preference { this.name = rec.referenceId; this.preferenceId = rec.status }
        }
        stub.mapPreferenceImportColumns(request)
    }
}

/** Sends records to acme.keymanagement.v1.CryptoKeyService. */
class RelayServiceScheduleIdCryptoKey(private val stub: CryptoKeyServiceGrpcKt.CryptoKeyServiceCoroutineStub) : RelayServiceScheduleIdTarget {
    override suspend fun forward(rec: RelayServiceScheduleIdRecord) {
        stub.getCryptoKey(getCryptoKeyRequest { this.cryptoKeyId = rec.referenceId })
    }
}
