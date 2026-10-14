package com.acme.transport.ops.networkmirror

import com.acme.customers.v1.CustomerSegmentReportingServiceGrpcKt
import com.acme.customers.v1.deleteCustomerSegmentReportRequest
import com.acme.routing.v1.PreviewStopRequestKt
import com.acme.routing.v1.StopValidationServiceGrpcKt
import com.acme.routing.v1.previewStopRequest
import com.acme.routing.v1.stop
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.workplaces.v1.BuildingQueryServiceGrpcKt
import com.acme.workplaces.v1.suggestBuildingsRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.workplaces.v1.BuildingQueryService/SuggestBuildings and hands what it keeps to the sink of its kind. */
suspend fun forwardBuildingsToken(channel: ManagedChannel) {
    val upstream = BuildingQueryServiceGrpcKt.BuildingQueryServiceCoroutineStub(channel)
    val got = RpcBoundary.read { upstream.suggestBuildings(suggestBuildingsRequest { this.orderBy = "main-forward-buildings-token" }) }
    val row = ForwardBuildingsTokenRecord(
        nextPageToken = got.pageInfo.nextPageToken,
        correlationId = got.result.correlationId,
        message = normalize(got.result.message),
        status = got.result.status,
    )
    dispatchForwardBuildingsToken("stop-validation", row, channel)
}

/** Where forwardBuildingsToken hands each record. */
interface ForwardBuildingsTokenDestination {
    suspend fun forward(rec: ForwardBuildingsTokenRecord)
}

/** What forwardBuildingsToken keeps of each SuggestBuildings response. */
data class ForwardBuildingsTokenRecord(val nextPageToken: String, val correlationId: String, val message: String, val status: String)

/** Sends records to acme.customers.v1.CustomerSegmentReportingService. */
class ForwardBuildingsTokenCustomerSegmentReporting(private val stub: CustomerSegmentReportingServiceGrpcKt.CustomerSegmentReportingServiceCoroutineStub) : ForwardBuildingsTokenDestination {
    override suspend fun forward(rec: ForwardBuildingsTokenRecord) {
        stub.deleteCustomerSegmentReport(deleteCustomerSegmentReportRequest { this.customerSegmentId = rec.correlationId })
    }
}

/** Hands records to acme.routing.v1.StopValidationService. */
class ForwardBuildingsTokenStopValidation(private val stub: StopValidationServiceGrpcKt.StopValidationServiceCoroutineStub) : ForwardBuildingsTokenDestination {
    override suspend fun forward(rec: ForwardBuildingsTokenRecord) {
        val request = previewStopRequest {
            this.stop = stop { this.stopId = rec.message; this.title = rec.status }
            this.details = PreviewStopRequestKt.details { this.locale = rec.nextPageToken }
        }
        stub.previewStop(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class ForwardBuildingsTokenLogOnly : ForwardBuildingsTokenDestination {
    override suspend fun forward(rec: ForwardBuildingsTokenRecord) {
        LoggerFactory.getLogger("ForwardBuildingsTokenRecord").atInfo().addKeyValue("nextPageToken", rec.nextPageToken).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchForwardBuildingsToken(kind: String, rec: ForwardBuildingsTokenRecord, channel: ManagedChannel) {
    var write: suspend (ForwardBuildingsTokenRecord) -> Unit = ForwardBuildingsTokenLogOnly()::forward
    when (kind) {
        "customer-segment-reporting" -> write = ForwardBuildingsTokenCustomerSegmentReporting(CustomerSegmentReportingServiceGrpcKt.CustomerSegmentReportingServiceCoroutineStub(channel))::forward
        "stop-validation" -> write = ForwardBuildingsTokenStopValidation(StopValidationServiceGrpcKt.StopValidationServiceCoroutineStub(channel))::forward
    }
    write(rec)
}
