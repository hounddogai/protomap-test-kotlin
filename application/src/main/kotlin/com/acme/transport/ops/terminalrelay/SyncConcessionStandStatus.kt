package com.acme.transport.ops.terminalrelay

import com.acme.concessions.v1.ConcessionStandServiceGrpcKt
import com.acme.concessions.v1.updateConcessionStandRequest
import com.acme.corporateevents.v1.CorporateEventCalculationServiceGrpcKt
import com.acme.corporateevents.v1.corporateEvent
import com.acme.corporateevents.v1.estimateCorporateEventRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.concessions.v1.ConcessionStandService/UpdateConcessionStand and hands what it keeps to acme.corporateevents.v1.CorporateEventCalculationService. */
suspend fun syncConcessionStandStatus(channel: ManagedChannel) {
    val reader = ConcessionStandServiceGrpcKt.ConcessionStandServiceCoroutineStub(channel)
    val reply = reader.updateConcessionStand(updateConcessionStandRequest { })
    val rec = SyncConcessionStandStatusRecord(
        status = normalize(reply.result.status),
        code = normalize(reply.concessionStand.code),
        message = normalize(reply.result.message),
    )
    SyncConcessionStandStatusCorporateEventCalculation(CorporateEventCalculationServiceGrpcKt.CorporateEventCalculationServiceCoroutineStub(channel)).transmit(rec)
}

/** What syncConcessionStandStatus keeps of each UpdateConcessionStand response. */
data class SyncConcessionStandStatusRecord(val status: String, val code: String, val message: String)

/** Hands records to acme.corporateevents.v1.CorporateEventCalculationService. */
class SyncConcessionStandStatusCorporateEventCalculation(private val stub: CorporateEventCalculationServiceGrpcKt.CorporateEventCalculationServiceCoroutineStub) {
    suspend fun transmit(rec: SyncConcessionStandStatusRecord) {
        val request = estimateCorporateEventRequest {
            this.corporateEvent = corporateEvent { this.corporateEventId = rec.status }
            this.reason = rec.code
        }
        stub.estimateCorporateEvent(request)
    }
}
