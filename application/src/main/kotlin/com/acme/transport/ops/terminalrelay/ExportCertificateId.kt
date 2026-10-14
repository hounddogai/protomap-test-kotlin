package com.acme.transport.ops.terminalrelay

import com.acme.certificates.v1.CertificateQueryServiceGrpcKt
import com.acme.certificates.v1.lookupCertificateRequest
import com.acme.common.v1.pageRequest
import com.acme.deliveries.v1.DeliveryExceptionWorkflowServiceGrpcKt
import com.acme.deliveries.v1.listDeliveryExceptionWorkflowsRequest
import com.acme.fleet.v1.AdvanceVehicleWorkflowRequestKt
import com.acme.fleet.v1.VehicleWorkflowServiceGrpcKt
import com.acme.fleet.v1.advanceVehicleWorkflowRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.peekVenueRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.certificates.v1.CertificateQueryService/LookupCertificate and hands what it keeps to the sink of its kind. */
suspend fun exportCertificateId(channel: ManagedChannel) {
    val source = CertificateQueryServiceGrpcKt.CertificateQueryServiceCoroutineStub(channel)
    val current = RpcBoundary.read { source.lookupCertificate(lookupCertificateRequest { this.certificateId = "current-export-certificate-id" }) }
    val row = ExportCertificateIdRecord(
        correlationId = current.result.correlationId,
        certificateId = current.certificate.certificateId,
    )
    dispatchExportCertificateId("vehicle-workflow", row, channel)
}

/** Where exportCertificateId hands each record. */
interface ExportCertificateIdTarget {
    suspend fun relay(rec: ExportCertificateIdRecord)
}

/** What exportCertificateId keeps of each LookupCertificate response. */
data class ExportCertificateIdRecord(val correlationId: String, val certificateId: String)

/** Sends records to acme.venues.v1.VenueQueryService. */
class ExportCertificateIdVenueQuery(private val stub: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub) : ExportCertificateIdTarget {
    override suspend fun relay(rec: ExportCertificateIdRecord) {
        stub.peekVenue(peekVenueRequest { this.venueId = rec.certificateId })
    }
}

/** Passes records to acme.deliveries.v1.DeliveryExceptionWorkflowService. */
class ExportCertificateIdDeliveryExceptionWorkflow(private val stub: DeliveryExceptionWorkflowServiceGrpcKt.DeliveryExceptionWorkflowServiceCoroutineStub) : ExportCertificateIdTarget {
    override suspend fun relay(rec: ExportCertificateIdRecord) {
        val request = listDeliveryExceptionWorkflowsRequest {
            this.page = pageRequest { this.pageToken = rec.correlationId }
        }
        stub.listDeliveryExceptionWorkflows(request)
    }
}

/** Writes records to acme.fleet.v1.VehicleWorkflowService. */
class ExportCertificateIdVehicleWorkflow(private val stub: VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub) : ExportCertificateIdTarget {
    override suspend fun relay(rec: ExportCertificateIdRecord) {
        val request = advanceVehicleWorkflowRequest {
            this.reason = rec.correlationId
            this.details = AdvanceVehicleWorkflowRequestKt.details { this.step = rec.certificateId }
        }
        RpcBoundary.run("acme.fleet.v1.VehicleWorkflowService/AdvanceVehicleWorkflow") { stub.advanceVehicleWorkflow(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class ExportCertificateIdLogOnly : ExportCertificateIdTarget {
    override suspend fun relay(rec: ExportCertificateIdRecord) {
        LoggerFactory.getLogger("ExportCertificateIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchExportCertificateId(kind: String, rec: ExportCertificateIdRecord, channel: ManagedChannel) {
    var write: suspend (ExportCertificateIdRecord) -> Unit = ExportCertificateIdLogOnly()::relay
    when (kind) {
        "venue-query" -> write = ExportCertificateIdVenueQuery(VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub(channel))::relay
        "delivery-exception-workflow" -> write = ExportCertificateIdDeliveryExceptionWorkflow(DeliveryExceptionWorkflowServiceGrpcKt.DeliveryExceptionWorkflowServiceCoroutineStub(channel))::relay
        "vehicle-workflow" -> write = ExportCertificateIdVehicleWorkflow(VehicleWorkflowServiceGrpcKt.VehicleWorkflowServiceCoroutineStub(channel))::relay
    }
    write(rec)
}
