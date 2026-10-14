package com.acme.transport.ops.emissionspublisher

import com.acme.broadcasts.v1.BroadcastServiceGrpcKt
import com.acme.broadcasts.v1.SearchBroadcastsRequestKt
import com.acme.broadcasts.v1.searchBroadcastsRequest
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.FindDocumentsRequestKt
import com.acme.documents.v1.findDocumentsRequest
import com.acme.telematics.v1.TripLogReportingServiceGrpcKt
import com.acme.telematics.v1.countTripLogTotalsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.telematics.v1.TripLogReportingService/CountTripLogTotals and hands what it keeps to the sink of its kind. */
suspend fun propagateTripLogTotalsMessage(channel: ManagedChannel) {
    val upstream = TripLogReportingServiceGrpcKt.TripLogReportingServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.countTripLogTotals(countTripLogTotalsRequest { this.orderBy = "primary-propagate-trip-log-totals-message" }) }
    val entry = PropagateTripLogTotalsMessageRecord(
        message = reply.result.message,
        status = reply.result.status,
    )
    deliverPropagateTripLogTotalsMessage("document-query", entry, channel)
}

/** Where propagateTripLogTotalsMessage hands each record. */
interface PropagateTripLogTotalsMessageTarget {
    suspend fun transmit(rec: PropagateTripLogTotalsMessageRecord)
}

/** What propagateTripLogTotalsMessage keeps of each CountTripLogTotals response. */
data class PropagateTripLogTotalsMessageRecord(val message: String, val status: String)

/** Hands records to acme.documents.v1.DocumentQueryService. */
class PropagateTripLogTotalsMessageDocumentQuery(private val stub: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub) : PropagateTripLogTotalsMessageTarget {
    override suspend fun transmit(rec: PropagateTripLogTotalsMessageRecord) {
        val request = findDocumentsRequest {
            this.filter = FindDocumentsRequestKt.filter { this.region = rec.message; this.query = rec.status }
        }
        stub.findDocuments(request)
    }
}

/** Writes records to acme.broadcasts.v1.BroadcastService. */
class PropagateTripLogTotalsMessageBroadcast(private val stub: BroadcastServiceGrpcKt.BroadcastServiceCoroutineStub) : PropagateTripLogTotalsMessageTarget {
    override suspend fun transmit(rec: PropagateTripLogTotalsMessageRecord) {
        val request = searchBroadcastsRequest {
            this.filter = SearchBroadcastsRequestKt.filter { this.ownerId = rec.message; this.region = rec.status }
        }
        stub.searchBroadcasts(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class PropagateTripLogTotalsMessageLogOnly : PropagateTripLogTotalsMessageTarget {
    override suspend fun transmit(rec: PropagateTripLogTotalsMessageRecord) {
        LoggerFactory.getLogger("PropagateTripLogTotalsMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverPropagateTripLogTotalsMessage(kind: String, rec: PropagateTripLogTotalsMessageRecord, channel: ManagedChannel) {
    var sink: PropagateTripLogTotalsMessageTarget = PropagateTripLogTotalsMessageLogOnly()
    when (kind) {
        "document-query" -> sink = PropagateTripLogTotalsMessageDocumentQuery(DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub(channel))
        "broadcast" -> sink = PropagateTripLogTotalsMessageBroadcast(BroadcastServiceGrpcKt.BroadcastServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
