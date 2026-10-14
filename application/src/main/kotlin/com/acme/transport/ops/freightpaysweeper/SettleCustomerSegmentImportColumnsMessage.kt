package com.acme.transport.ops.freightpaysweeper

import com.acme.broadcasts.v1.BroadcastServiceGrpcKt
import com.acme.broadcasts.v1.broadcast
import com.acme.broadcasts.v1.cloneBroadcastRequest
import com.acme.customers.v1.ConsentRecordSyncServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentImportServiceGrpcKt
import com.acme.customers.v1.PushConsentRecordRequestKt
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.mapCustomerSegmentImportColumnsRequest
import com.acme.customers.v1.pushConsentRecordRequest
import com.acme.ticketing.v1.TicketServiceGrpcKt
import com.acme.ticketing.v1.cloneTicketRequest
import com.acme.ticketing.v1.ticket
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.CustomerSegmentImportService/MapCustomerSegmentImportColumns and hands what it keeps to the sink of its kind. */
suspend fun settleCustomerSegmentImportColumnsMessage(channel: ManagedChannel) {
    val reader = CustomerSegmentImportServiceGrpcKt.CustomerSegmentImportServiceCoroutineStub(channel)
    val current = reader.mapCustomerSegmentImportColumns(mapCustomerSegmentImportColumnsRequest { this.customerSegmentId = "current-settle-customer-segment-import-columns-message" })
    val entry = SettleCustomerSegmentImportColumnsMessageRecord(
        message = current.outcome.message,
        correlationId = current.result.correlationId,
        status = current.result.status,
    )
    val registry = mutableMapOf<String, SettleCustomerSegmentImportColumnsMessageDestination>()
    for (registration in listOf(
        SettleCustomerSegmentImportColumnsMessageRegistration("ticket", SettleCustomerSegmentImportColumnsMessageTicket(TicketServiceGrpcKt.TicketServiceCoroutineStub(channel))),
        SettleCustomerSegmentImportColumnsMessageRegistration("broadcast", SettleCustomerSegmentImportColumnsMessageBroadcast(BroadcastServiceGrpcKt.BroadcastServiceCoroutineStub(channel))),
        SettleCustomerSegmentImportColumnsMessageRegistration("consent-record-sync", SettleCustomerSegmentImportColumnsMessageConsentRecordSync(ConsentRecordSyncServiceGrpcKt.ConsentRecordSyncServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["consent-record-sync"] ?: return
    sink.hand(entry)
}

/** Where settleCustomerSegmentImportColumnsMessage hands each record. */
interface SettleCustomerSegmentImportColumnsMessageDestination {
    suspend fun hand(rec: SettleCustomerSegmentImportColumnsMessageRecord)
}

/** What settleCustomerSegmentImportColumnsMessage keeps of each MapCustomerSegmentImportColumns response. */
data class SettleCustomerSegmentImportColumnsMessageRecord(val message: String, val correlationId: String, val status: String)

/** Passes records to acme.ticketing.v1.TicketService. */
class SettleCustomerSegmentImportColumnsMessageTicket(private val stub: TicketServiceGrpcKt.TicketServiceCoroutineStub) : SettleCustomerSegmentImportColumnsMessageDestination {
    override suspend fun hand(rec: SettleCustomerSegmentImportColumnsMessageRecord) {
        val request = cloneTicketRequest {
            this.requestId = rec.status
            this.ticket = ticket { this.notes = rec.correlationId; this.number = rec.message }
        }
        RpcBoundary.run("acme.ticketing.v1.TicketService/CloneTicket") { stub.cloneTicket(request) }
    }
}

/** Passes records to acme.broadcasts.v1.BroadcastService. */
class SettleCustomerSegmentImportColumnsMessageBroadcast(private val stub: BroadcastServiceGrpcKt.BroadcastServiceCoroutineStub) : SettleCustomerSegmentImportColumnsMessageDestination {
    override suspend fun hand(rec: SettleCustomerSegmentImportColumnsMessageRecord) {
        stub.cloneBroadcast(cloneBroadcastRequest { this.broadcast = broadcast { this.title = rec.message } })
    }
}

/** Passes records to acme.customers.v1.ConsentRecordSyncService. */
class SettleCustomerSegmentImportColumnsMessageConsentRecordSync(private val stub: ConsentRecordSyncServiceGrpcKt.ConsentRecordSyncServiceCoroutineStub) : SettleCustomerSegmentImportColumnsMessageDestination {
    override suspend fun hand(rec: SettleCustomerSegmentImportColumnsMessageRecord) {
        val request = pushConsentRecordRequest {
            this.consentRecord = consentRecord { this.consentRecordId = rec.status }
            this.details = PushConsentRecordRequestKt.details { this.peerRegion = rec.message }
            this.consentRecordId = rec.correlationId
        }
        stub.pushConsentRecord(request)
    }
}

/** A sink and the kind of record it takes. */
class SettleCustomerSegmentImportColumnsMessageRegistration(val kind: String, val sink: SettleCustomerSegmentImportColumnsMessageDestination)
