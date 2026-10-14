package com.acme.transport.ops.fleetopsreconciler

import com.acme.deliveries.v1.ProofOfDeliveryCalculationServiceGrpcKt
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.simulateProofOfDeliveryRequest
import com.acme.maintenance.v1.PartReportingServiceGrpcKt
import com.acme.maintenance.v1.sharePartReportRequest
import com.acme.telematics.v1.TripLogImportServiceGrpcKt
import com.acme.telematics.v1.retryTripLogImportRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.PartReportingService/SharePartReport and hands what it keeps to the sink of its kind. */
suspend fun copyPartReportId(channel: ManagedChannel) {
    val origin = PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.sharePartReport(sharePartReportRequest { this.partId = "default-copy-part-report-id" }) }
    val record = CopyPartReportIdRecord(
        referenceId = got.outcome.referenceId,
        code = got.part.code,
    )
    actOnCopyPartReportId("trip-log-import", record, channel)
}

/** Where copyPartReportId hands each record. */
interface CopyPartReportIdDestination {
    suspend fun transmit(rec: CopyPartReportIdRecord)
}

/** What copyPartReportId keeps of each SharePartReport response. */
data class CopyPartReportIdRecord(val referenceId: String, val code: String)

/** Writes records to acme.telematics.v1.TripLogImportService. */
class CopyPartReportIdTripLogImport(private val stub: TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub) : CopyPartReportIdDestination {
    override suspend fun transmit(rec: CopyPartReportIdRecord) {
        val request = retryTripLogImportRequest {
            this.tripLog = tripLog { this.number = rec.referenceId; this.tripLogId = rec.code }
        }
        stub.retryTripLogImport(request)
    }
}

/** Passes records to acme.deliveries.v1.ProofOfDeliveryCalculationService. */
class CopyPartReportIdProofOfDeliveryCalculation(private val stub: ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub) : CopyPartReportIdDestination {
    override suspend fun transmit(rec: CopyPartReportIdRecord) {
        val request = simulateProofOfDeliveryRequest {
            this.proofOfDelivery = proofOfDelivery { this.number = rec.code }
        }
        stub.simulateProofOfDelivery(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnCopyPartReportId(kind: String, rec: CopyPartReportIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "trip-log-import" to { CopyPartReportIdTripLogImport(TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub(channel)).transmit(rec) },
        "proof-of-delivery-calculation" to { CopyPartReportIdProofOfDeliveryCalculation(ProofOfDeliveryCalculationServiceGrpcKt.ProofOfDeliveryCalculationServiceCoroutineStub(channel)).transmit(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
