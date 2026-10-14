package com.acme.transport.ops.terminalrelay

import com.acme.certificates.v1.CertificateServiceGrpcKt
import com.acme.certificates.v1.SearchCertificatesRequestKt
import com.acme.certificates.v1.searchCertificatesRequest
import com.acme.common.v1.pageRequest
import com.acme.telematics.v1.GeoFenceApprovalServiceGrpcKt
import com.acme.telematics.v1.GeoFenceQueryServiceGrpcKt
import com.acme.telematics.v1.SubmitGeoFenceRequestKt
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.deleteTelemetryReadingRequest
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.submitGeoFenceRequest
import com.acme.telematics.v1.suggestGeoFencesRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.GeoFenceQueryService/SuggestGeoFences and hands what it keeps to the sink of its kind. */
suspend fun routeGeoFencesStatus(channel: ManagedChannel) {
    val upstream = GeoFenceQueryServiceGrpcKt.GeoFenceQueryServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.suggestGeoFences(suggestGeoFencesRequest { this.orderBy = "nightly-route-geo-fences-status" }) }
    val row = RouteGeoFencesStatusRecord(
        status = reply.result.status,
        correlationId = reply.result.correlationId,
        nextPageToken = reply.pageInfo.nextPageToken,
        message = reply.result.message,
    )
    actOnRouteGeoFencesStatus("geo-fence-approval", row, channel)
}

/** Where routeGeoFencesStatus hands each record. */
interface RouteGeoFencesStatusSink {
    suspend fun deliver(rec: RouteGeoFencesStatusRecord)
}

/** What routeGeoFencesStatus keeps of each SuggestGeoFences response. */
data class RouteGeoFencesStatusRecord(val status: String, val correlationId: String, val nextPageToken: String, val message: String)

/** Writes records to acme.telematics.v1.TelemetryReadingService. */
class RouteGeoFencesStatusTelemetryReading(private val stub: TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub) : RouteGeoFencesStatusSink {
    override suspend fun deliver(rec: RouteGeoFencesStatusRecord) {
        val request = deleteTelemetryReadingRequest {
            this.telemetryReadingId = rec.correlationId
            this.etag = rec.status
        }
        stub.deleteTelemetryReading(request)
    }
}

/** Passes records to acme.certificates.v1.CertificateService. */
class RouteGeoFencesStatusCertificate(private val stub: CertificateServiceGrpcKt.CertificateServiceCoroutineStub) : RouteGeoFencesStatusSink {
    override suspend fun deliver(rec: RouteGeoFencesStatusRecord) {
        val request = searchCertificatesRequest {
            this.filter = SearchCertificatesRequestKt.filter { this.query = rec.message; this.region = rec.correlationId }
            this.page = pageRequest { this.pageToken = rec.nextPageToken }
        }
        RpcBoundary.run("acme.certificates.v1.CertificateService/SearchCertificates") { stub.searchCertificates(request) }
    }
}

/** Hands records to acme.telematics.v1.GeoFenceApprovalService. */
class RouteGeoFencesStatusGeoFenceApproval(private val stub: GeoFenceApprovalServiceGrpcKt.GeoFenceApprovalServiceCoroutineStub) : RouteGeoFencesStatusSink {
    override suspend fun deliver(rec: RouteGeoFencesStatusRecord) {
        val request = submitGeoFenceRequest {
            this.details = SubmitGeoFenceRequestKt.details { this.comment = rec.nextPageToken }
            this.geoFence = geoFence { this.description = rec.correlationId }
        }
        stub.submitGeoFence(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnRouteGeoFencesStatus(kind: String, rec: RouteGeoFencesStatusRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "telemetry-reading" to { RouteGeoFencesStatusTelemetryReading(TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub(channel)).deliver(rec) },
        "certificate" to { RouteGeoFencesStatusCertificate(CertificateServiceGrpcKt.CertificateServiceCoroutineStub(channel)).deliver(rec) },
        "geo-fence-approval" to { RouteGeoFencesStatusGeoFenceApproval(GeoFenceApprovalServiceGrpcKt.GeoFenceApprovalServiceCoroutineStub(channel)).deliver(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
