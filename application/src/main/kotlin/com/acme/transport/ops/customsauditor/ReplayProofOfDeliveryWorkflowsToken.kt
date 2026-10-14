package com.acme.transport.ops.customsauditor

import com.acme.certificates.v1.CertificateCalculationServiceGrpcKt
import com.acme.certificates.v1.calculateCertificateRequest
import com.acme.certificates.v1.certificate
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.listProofOfDeliveryWorkflowsRequest
import com.acme.maintenance.v1.SimulateWorkOrderRequestKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.simulateWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.telematics.v1.GeoFenceSyncServiceGrpcKt
import com.acme.telematics.v1.getGeoFenceSyncStateRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.deliveries.v1.ProofOfDeliveryWorkflowService/ListProofOfDeliveryWorkflows and hands what it keeps to the sink of its kind. */
suspend fun replayProofOfDeliveryWorkflowsToken(channel: ManagedChannel) {
    val source = ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub(channel)
    val current = RpcBoundary.read { source.listProofOfDeliveryWorkflows(listProofOfDeliveryWorkflowsRequest { this.orderBy = "main-replay-proof-of-delivery-workflows-token" }) }
    val row = ReplayProofOfDeliveryWorkflowsTokenRecord(
        nextPageToken = current.pageInfo.nextPageToken,
        status = normalize(current.result.status),
        message = normalize(current.result.message),
    )
    dispatchReplayProofOfDeliveryWorkflowsToken("work-order-calculation", row, channel)
}

/** Where replayProofOfDeliveryWorkflowsToken hands each record. */
interface ReplayProofOfDeliveryWorkflowsTokenTarget {
    suspend fun hand(rec: ReplayProofOfDeliveryWorkflowsTokenRecord)
}

/** What replayProofOfDeliveryWorkflowsToken keeps of each ListProofOfDeliveryWorkflows response. */
data class ReplayProofOfDeliveryWorkflowsTokenRecord(val nextPageToken: String, val status: String, val message: String)

/** Writes records to acme.certificates.v1.CertificateCalculationService. */
class ReplayProofOfDeliveryWorkflowsTokenCertificateCalculation(private val stub: CertificateCalculationServiceGrpcKt.CertificateCalculationServiceCoroutineStub) : ReplayProofOfDeliveryWorkflowsTokenTarget {
    override suspend fun hand(rec: ReplayProofOfDeliveryWorkflowsTokenRecord) {
        val request = calculateCertificateRequest {
            this.certificate = certificate { this.number = rec.message }
            this.certificateId = rec.status
        }
        stub.calculateCertificate(request)
    }
}

/** Passes records to acme.maintenance.v1.WorkOrderCalculationService. */
class ReplayProofOfDeliveryWorkflowsTokenWorkOrderCalculation(private val stub: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub) : ReplayProofOfDeliveryWorkflowsTokenTarget {
    override suspend fun hand(rec: ReplayProofOfDeliveryWorkflowsTokenRecord) {
        val request = simulateWorkOrderRequest {
            this.details = SimulateWorkOrderRequestKt.details { this.currencyCode = rec.nextPageToken }
            this.workOrder = workOrder { this.workOrderId = rec.message }
        }
        RpcBoundary.run("acme.maintenance.v1.WorkOrderCalculationService/SimulateWorkOrder") { stub.simulateWorkOrder(request) }
    }
}

/** Passes records to acme.telematics.v1.GeoFenceSyncService. */
class ReplayProofOfDeliveryWorkflowsTokenGeoFenceSync(private val stub: GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub) : ReplayProofOfDeliveryWorkflowsTokenTarget {
    override suspend fun hand(rec: ReplayProofOfDeliveryWorkflowsTokenRecord) {
        RpcBoundary.run("acme.telematics.v1.GeoFenceSyncService/GetGeoFenceSyncState") { stub.getGeoFenceSyncState(getGeoFenceSyncStateRequest { this.geoFenceId = rec.nextPageToken }) }
    }
}

/** Only logs, for kinds no service takes yet. */
class ReplayProofOfDeliveryWorkflowsTokenLogOnly : ReplayProofOfDeliveryWorkflowsTokenTarget {
    override suspend fun hand(rec: ReplayProofOfDeliveryWorkflowsTokenRecord) {
        LoggerFactory.getLogger("ReplayProofOfDeliveryWorkflowsTokenRecord").atInfo().addKeyValue("nextPageToken", rec.nextPageToken).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchReplayProofOfDeliveryWorkflowsToken(kind: String, rec: ReplayProofOfDeliveryWorkflowsTokenRecord, channel: ManagedChannel) {
    var write: suspend (ReplayProofOfDeliveryWorkflowsTokenRecord) -> Unit = ReplayProofOfDeliveryWorkflowsTokenLogOnly()::hand
    when (kind) {
        "certificate-calculation" -> write = ReplayProofOfDeliveryWorkflowsTokenCertificateCalculation(CertificateCalculationServiceGrpcKt.CertificateCalculationServiceCoroutineStub(channel))::hand
        "work-order-calculation" -> write = ReplayProofOfDeliveryWorkflowsTokenWorkOrderCalculation(WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub(channel))::hand
        "geo-fence-sync" -> write = ReplayProofOfDeliveryWorkflowsTokenGeoFenceSync(GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub(channel))::hand
    }
    write(rec)
}
