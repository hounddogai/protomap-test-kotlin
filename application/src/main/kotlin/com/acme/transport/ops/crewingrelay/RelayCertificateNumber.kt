package com.acme.transport.ops.crewingrelay

import com.acme.certificates.v1.CertificateCalculationServiceGrpcKt
import com.acme.certificates.v1.quoteCertificateRequest
import com.acme.drivers.v1.DutyStatusReportingServiceGrpcKt
import com.acme.drivers.v1.shareDutyStatusReportRequest
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.route
import com.acme.routing.v1.signalRouteWorkflowRequest
import io.grpc.ManagedChannel

/** Reads acme.certificates.v1.CertificateCalculationService/QuoteCertificate and hands what it keeps to the sink of its kind. */
suspend fun relayCertificateNumber(channel: ManagedChannel) {
    val origin = CertificateCalculationServiceGrpcKt.CertificateCalculationServiceCoroutineStub(channel)
    val got = origin.quoteCertificate(quoteCertificateRequest { this.certificateId = "current-relay-certificate-number" })
    val row = RelayCertificateNumberRecord(
        number = got.certificate.number,
        certificateId = got.certificate.certificateId,
    )
    val sinks = mapOf<String, RelayCertificateNumberTarget>(
        "route-workflow" to RelayCertificateNumberRouteWorkflow(RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub(channel)),
        "duty-status-reporting" to RelayCertificateNumberDutyStatusReporting(DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("RELAY_CERTIFICATE_NUMBER_TARGET")] ?: return
    sink.hand(row)
}

/** Where relayCertificateNumber hands each record. */
interface RelayCertificateNumberTarget {
    suspend fun hand(rec: RelayCertificateNumberRecord)
}

/** What relayCertificateNumber keeps of each QuoteCertificate response. */
data class RelayCertificateNumberRecord(val number: String, val certificateId: String)

/** Passes records to acme.routing.v1.RouteWorkflowService. */
class RelayCertificateNumberRouteWorkflow(private val stub: RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub) : RelayCertificateNumberTarget {
    override suspend fun hand(rec: RelayCertificateNumberRecord) {
        val request = signalRouteWorkflowRequest {
            this.route = route { this.routeId = rec.certificateId }
            this.routeId = rec.number
        }
        stub.signalRouteWorkflow(request)
    }
}

/** Writes records to acme.drivers.v1.DutyStatusReportingService. */
class RelayCertificateNumberDutyStatusReporting(private val stub: DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub) : RelayCertificateNumberTarget {
    override suspend fun hand(rec: RelayCertificateNumberRecord) {
        stub.shareDutyStatusReport(shareDutyStatusReportRequest { this.reason = rec.number })
    }
}
