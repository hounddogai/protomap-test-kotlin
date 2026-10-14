package com.acme.transport.ops.crewingreconciler

import com.acme.audiences.v1.AudienceQueryServiceGrpcKt
import com.acme.audiences.v1.browseAudiencesRequest
import com.acme.deliveries.v1.ProofOfDeliveryValidationServiceGrpcKt
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.validateProofOfDeliveryRequest
import com.acme.drivers.v1.CalculateDriverLicenseRequestKt
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.calculateDriverLicenseRequest
import com.acme.drivers.v1.driverLicense
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.NormalizeYardMoveRequestKt
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.normalizeYardMoveRequest
import com.acme.yards.v1.yardMove
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.audiences.v1.AudienceQueryService/BrowseAudiences and hands what it keeps to the sink of its kind. */
suspend fun syncAudiencesStatus(channel: ManagedChannel) {
    val source = AudienceQueryServiceGrpcKt.AudienceQueryServiceCoroutineStub(channel)
    val found = RpcBoundary.read { source.browseAudiences(browseAudiencesRequest { this.orderBy = "nightly-sync-audiences-status" }) }
    val row = SyncAudiencesStatusRecord(
        status = found.result.status,
        nextPageToken = found.pageInfo.nextPageToken,
        message = normalize(found.result.message),
        correlationId = found.result.correlationId,
    )
    deliverSyncAudiencesStatus("driver-license-calculation", row, channel)
}

/** Where syncAudiencesStatus hands each record. */
interface SyncAudiencesStatusTarget {
    suspend fun hand(rec: SyncAudiencesStatusRecord)
}

/** What syncAudiencesStatus keeps of each BrowseAudiences response. */
data class SyncAudiencesStatusRecord(val status: String, val nextPageToken: String, val message: String, val correlationId: String)

/** Writes records to acme.yards.v1.YardMoveValidationService. */
class SyncAudiencesStatusYardMoveValidation(private val stub: YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub) : SyncAudiencesStatusTarget {
    override suspend fun hand(rec: SyncAudiencesStatusRecord) {
        val request = normalizeYardMoveRequest {
            this.details = NormalizeYardMoveRequestKt.details { this.locale = rec.message }
            this.yardMoveId = rec.correlationId
            this.yardMove = yardMove { this.yardMoveId = rec.nextPageToken }
        }
        stub.normalizeYardMove(request)
    }
}

/** Sends records to acme.drivers.v1.DriverLicenseCalculationService. */
class SyncAudiencesStatusDriverLicenseCalculation(private val stub: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub) : SyncAudiencesStatusTarget {
    override suspend fun hand(rec: SyncAudiencesStatusRecord) {
        val request = calculateDriverLicenseRequest {
            this.driverLicense = driverLicense { this.notes = rec.status; this.driverLicenseId = rec.nextPageToken }
            this.details = CalculateDriverLicenseRequestKt.details { this.currencyCode = rec.correlationId }
        }
        stub.calculateDriverLicense(request)
    }
}

/** Writes records to acme.deliveries.v1.ProofOfDeliveryValidationService. */
class SyncAudiencesStatusProofOfDeliveryValidation(private val stub: ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub) : SyncAudiencesStatusTarget {
    override suspend fun hand(rec: SyncAudiencesStatusRecord) {
        val request = validateProofOfDeliveryRequest {
            this.proofOfDelivery = proofOfDelivery { this.proofOfDeliveryId = rec.nextPageToken; this.number = rec.message }
            this.proofOfDeliveryId = rec.status
        }
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryValidationService/ValidateProofOfDelivery") { stub.validateProofOfDelivery(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class SyncAudiencesStatusLogOnly : SyncAudiencesStatusTarget {
    override suspend fun hand(rec: SyncAudiencesStatusRecord) {
        LoggerFactory.getLogger("SyncAudiencesStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverSyncAudiencesStatus(kind: String, rec: SyncAudiencesStatusRecord, channel: ManagedChannel) {
    var sink: SyncAudiencesStatusTarget = SyncAudiencesStatusLogOnly()
    when (kind) {
        "yard-move-validation" -> sink = SyncAudiencesStatusYardMoveValidation(YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub(channel))
        "driver-license-calculation" -> sink = SyncAudiencesStatusDriverLicenseCalculation(DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub(channel))
        "proof-of-delivery-validation" -> sink = SyncAudiencesStatusProofOfDeliveryValidation(ProofOfDeliveryValidationServiceGrpcKt.ProofOfDeliveryValidationServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
