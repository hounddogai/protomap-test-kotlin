package com.acme.transport.ops.terminalsweeper

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.scoreAccessPolicyRequest
import com.acme.datasets.v1.DatasetServiceGrpcKt
import com.acme.datasets.v1.getDatasetRequest
import com.acme.maintenance.v1.PartReportingServiceGrpcKt
import com.acme.maintenance.v1.SharePartReportRequestKt
import com.acme.maintenance.v1.sharePartReportRequest
import com.acme.telehealth.v1.VideoVisitQueryServiceGrpcKt
import com.acme.telehealth.v1.resolveVideoVisitsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.telehealth.v1.VideoVisitQueryService/ResolveVideoVisits and hands what it keeps to the sink of its kind. */
suspend fun routeVideoVisitsMessage(channel: ManagedChannel) {
    val source = VideoVisitQueryServiceGrpcKt.VideoVisitQueryServiceCoroutineStub(channel)
    val found = source.resolveVideoVisits(resolveVideoVisitsRequest { })
    val row = RouteVideoVisitsMessageRecord(
        message = normalize(found.result.message),
        nextPageToken = found.pageInfo.nextPageToken,
        correlationId = found.result.correlationId,
    )
    dispatchRouteVideoVisitsMessage("part-reporting", row, channel)
}

/** Where routeVideoVisitsMessage hands each record. */
interface RouteVideoVisitsMessageTarget {
    suspend fun forward(rec: RouteVideoVisitsMessageRecord)
}

/** What routeVideoVisitsMessage keeps of each ResolveVideoVisits response. */
data class RouteVideoVisitsMessageRecord(val message: String, val nextPageToken: String, val correlationId: String)

/** Writes records to acme.maintenance.v1.PartReportingService. */
class RouteVideoVisitsMessagePartReporting(private val stub: PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub) : RouteVideoVisitsMessageTarget {
    override suspend fun forward(rec: RouteVideoVisitsMessageRecord) {
        val request = sharePartReportRequest {
            this.details = SharePartReportRequestKt.details { this.channel = rec.nextPageToken }
        }
        stub.sharePartReport(request)
    }
}

/** Writes records to acme.datasets.v1.DatasetService. */
class RouteVideoVisitsMessageDataset(private val stub: DatasetServiceGrpcKt.DatasetServiceCoroutineStub) : RouteVideoVisitsMessageTarget {
    override suspend fun forward(rec: RouteVideoVisitsMessageRecord) {
        RpcBoundary.run("acme.datasets.v1.DatasetService/GetDataset") { stub.getDataset(getDatasetRequest { this.datasetId = rec.correlationId }) }
    }
}

/** Hands records to acme.authorization.v1.AccessPolicyValidationService. */
class RouteVideoVisitsMessageAccessPolicyValidation(private val stub: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub) : RouteVideoVisitsMessageTarget {
    override suspend fun forward(rec: RouteVideoVisitsMessageRecord) {
        val request = scoreAccessPolicyRequest {
            this.accessPolicy = accessPolicy { this.name = rec.correlationId; this.accessPolicyId = rec.message }
        }
        stub.scoreAccessPolicy(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class RouteVideoVisitsMessageLogOnly : RouteVideoVisitsMessageTarget {
    override suspend fun forward(rec: RouteVideoVisitsMessageRecord) {
        LoggerFactory.getLogger("RouteVideoVisitsMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchRouteVideoVisitsMessage(kind: String, rec: RouteVideoVisitsMessageRecord, channel: ManagedChannel) {
    var write: suspend (RouteVideoVisitsMessageRecord) -> Unit = RouteVideoVisitsMessageLogOnly()::forward
    when (kind) {
        "part-reporting" -> write = RouteVideoVisitsMessagePartReporting(PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub(channel))::forward
        "dataset" -> write = RouteVideoVisitsMessageDataset(DatasetServiceGrpcKt.DatasetServiceCoroutineStub(channel))::forward
        "access-policy-validation" -> write = RouteVideoVisitsMessageAccessPolicyValidation(AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub(channel))::forward
    }
    write(rec)
}
