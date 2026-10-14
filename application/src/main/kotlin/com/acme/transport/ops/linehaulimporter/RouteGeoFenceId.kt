package com.acme.transport.ops.linehaulimporter

import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.NormalizeDispatchAssignmentRequestKt
import com.acme.dispatch.v1.normalizeDispatchAssignmentRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.createDocumentRequest
import com.acme.documents.v1.document
import com.acme.routing.v1.RouteApprovalServiceGrpcKt
import com.acme.routing.v1.commentRouteApprovalRequest
import com.acme.routing.v1.route
import com.acme.telematics.v1.GeoFenceLifecycleServiceGrpcKt
import com.acme.telematics.v1.reinstateGeoFenceRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.GeoFenceLifecycleService/ReinstateGeoFence and hands what it keeps to the sink of its kind. */
suspend fun routeGeoFenceId(channel: ManagedChannel) {
    val source = GeoFenceLifecycleServiceGrpcKt.GeoFenceLifecycleServiceCoroutineStub(channel)
    val got = source.reinstateGeoFence(reinstateGeoFenceRequest { this.geoFenceId = "main-route-geo-fence-id" })
    val record = RouteGeoFenceIdRecord(
        referenceId = normalize(got.outcome.referenceId),
        geoFenceId = got.geoFence.geoFenceId,
    )
    val sinks = mapOf<String, RouteGeoFenceIdSink>(
        "document" to RouteGeoFenceIdDocument(DocumentServiceGrpcKt.DocumentServiceCoroutineStub(channel)),
        "dispatch-assignment-validation" to RouteGeoFenceIdDispatchAssignmentValidation(DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub(channel)),
        "route-approval" to RouteGeoFenceIdRouteApproval(RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "route-approval") continue
        sinks[kind]?.forward(record)
    }
}

/** Where routeGeoFenceId hands each record. */
interface RouteGeoFenceIdSink {
    suspend fun forward(rec: RouteGeoFenceIdRecord)
}

/** What routeGeoFenceId keeps of each ReinstateGeoFence response. */
data class RouteGeoFenceIdRecord(val referenceId: String, val geoFenceId: String)

/** Writes records to acme.documents.v1.DocumentService. */
class RouteGeoFenceIdDocument(private val stub: DocumentServiceGrpcKt.DocumentServiceCoroutineStub) : RouteGeoFenceIdSink {
    override suspend fun forward(rec: RouteGeoFenceIdRecord) {
        val request = createDocumentRequest {
            this.document = document { this.number = rec.referenceId; this.documentId = rec.geoFenceId }
        }
        stub.createDocument(request)
    }
}

/** Hands records to acme.dispatch.v1.DispatchAssignmentValidationService. */
class RouteGeoFenceIdDispatchAssignmentValidation(private val stub: DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub) : RouteGeoFenceIdSink {
    override suspend fun forward(rec: RouteGeoFenceIdRecord) {
        val request = normalizeDispatchAssignmentRequest {
            this.details = NormalizeDispatchAssignmentRequestKt.details { this.locale = rec.geoFenceId }
            this.reason = rec.referenceId
        }
        stub.normalizeDispatchAssignment(request)
    }
}

/** Sends records to acme.routing.v1.RouteApprovalService. */
class RouteGeoFenceIdRouteApproval(private val stub: RouteApprovalServiceGrpcKt.RouteApprovalServiceCoroutineStub) : RouteGeoFenceIdSink {
    override suspend fun forward(rec: RouteGeoFenceIdRecord) {
        stub.commentRouteApproval(commentRouteApprovalRequest { this.route = route { this.number = rec.geoFenceId } })
    }
}
