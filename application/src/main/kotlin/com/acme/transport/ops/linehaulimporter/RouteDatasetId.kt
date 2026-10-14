package com.acme.transport.ops.linehaulimporter

import com.acme.certificates.v1.CertificateServiceGrpcKt
import com.acme.certificates.v1.deleteCertificateRequest
import com.acme.customers.v1.CustomerSegmentValidationServiceGrpcKt
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.previewCustomerSegmentRequest
import com.acme.datasets.v1.DatasetServiceGrpcKt
import com.acme.datasets.v1.deleteDatasetRequest
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.compareStopOptionsRequest
import com.acme.routing.v1.stop
import com.acme.ticketing.v1.TicketQueryServiceGrpcKt
import com.acme.ticketing.v1.lookupTicketRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.datasets.v1.DatasetService/DeleteDataset and hands what it keeps to the sink of its kind. */
suspend fun routeDatasetId(channel: ManagedChannel) {
    val upstream = DatasetServiceGrpcKt.DatasetServiceCoroutineStub(channel)
    val found = upstream.deleteDataset(deleteDatasetRequest { this.reason = "primary-route-dataset-id" })
    val rec = RouteDatasetIdRecord(
        correlationId = normalize(found.result.correlationId),
        message = found.result.message,
    )
    actOnRouteDatasetId("ticket-query", rec, channel)
}

/** Where routeDatasetId hands each record. */
interface RouteDatasetIdSink {
    suspend fun hand(rec: RouteDatasetIdRecord)
}

/** What routeDatasetId keeps of each DeleteDataset response. */
data class RouteDatasetIdRecord(val correlationId: String, val message: String)

/** Hands records to acme.customers.v1.CustomerSegmentValidationService. */
class RouteDatasetIdCustomerSegmentValidation(private val stub: CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub) : RouteDatasetIdSink {
    override suspend fun hand(rec: RouteDatasetIdRecord) {
        val request = previewCustomerSegmentRequest {
            this.customerSegment = customerSegment { this.name = rec.correlationId }
        }
        stub.previewCustomerSegment(request)
    }
}

/** Writes records to acme.ticketing.v1.TicketQueryService. */
class RouteDatasetIdTicketQuery(private val stub: TicketQueryServiceGrpcKt.TicketQueryServiceCoroutineStub) : RouteDatasetIdSink {
    override suspend fun hand(rec: RouteDatasetIdRecord) {
        stub.lookupTicket(lookupTicketRequest { this.ticketId = rec.correlationId })
    }
}

/** Hands records to acme.certificates.v1.CertificateService. */
class RouteDatasetIdCertificate(private val stub: CertificateServiceGrpcKt.CertificateServiceCoroutineStub) : RouteDatasetIdSink {
    override suspend fun hand(rec: RouteDatasetIdRecord) {
        val request = deleteCertificateRequest {
            this.certificateId = rec.correlationId
            this.reason = rec.message
        }
        RpcBoundary.run("acme.certificates.v1.CertificateService/DeleteCertificate") { stub.deleteCertificate(request) }
    }
}

/** Hands records to acme.routing.v1.StopCalculationService. */
class RouteDatasetIdStopCalculation(private val stub: StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub) : RouteDatasetIdSink {
    override suspend fun hand(rec: RouteDatasetIdRecord) {
        val request = compareStopOptionsRequest {
            this.stopId = rec.message
            this.stop = stop { this.stopId = rec.correlationId }
        }
        RpcBoundary.run("acme.routing.v1.StopCalculationService/CompareStopOptions") { stub.compareStopOptions(request) }
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnRouteDatasetId(kind: String, rec: RouteDatasetIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "customer-segment-validation" to { RouteDatasetIdCustomerSegmentValidation(CustomerSegmentValidationServiceGrpcKt.CustomerSegmentValidationServiceCoroutineStub(channel)).hand(rec) },
        "ticket-query" to { RouteDatasetIdTicketQuery(TicketQueryServiceGrpcKt.TicketQueryServiceCoroutineStub(channel)).hand(rec) },
        "certificate" to { RouteDatasetIdCertificate(CertificateServiceGrpcKt.CertificateServiceCoroutineStub(channel)).hand(rec) },
        "stop-calculation" to { RouteDatasetIdStopCalculation(StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub(channel)).hand(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
