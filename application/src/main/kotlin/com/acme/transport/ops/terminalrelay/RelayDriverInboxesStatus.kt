package com.acme.transport.ops.terminalrelay

import com.acme.common.v1.pageRequest
import com.acme.concessions.v1.ConcessionStandServiceGrpcKt
import com.acme.concessions.v1.searchConcessionStandsRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.createDocumentRequest
import com.acme.driverapp.v1.DriverInboxQueryServiceGrpcKt
import com.acme.driverapp.v1.countDriverInboxesRequest
import com.acme.sponsorships.v1.SponsorQueryServiceGrpcKt
import com.acme.sponsorships.v1.summarizeSponsorsRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.driverapp.v1.DriverInboxQueryService/CountDriverInboxes and hands what it keeps to the sink of its kind. */
suspend fun relayDriverInboxesStatus(channel: ManagedChannel) {
    val reader = DriverInboxQueryServiceGrpcKt.DriverInboxQueryServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { reader.countDriverInboxes(countDriverInboxesRequest { this.orderBy = "primary-relay-driver-inboxes-status" }) }
    val rec = RelayDriverInboxesStatusRecord(
        status = reply.result.status,
        correlationId = reply.result.correlationId,
        message = reply.result.message,
    )
    for (sink in listOf<RelayDriverInboxesStatusSink>(
        RelayDriverInboxesStatusSponsorQuery(SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub(channel)),
        RelayDriverInboxesStatusConcessionStand(ConcessionStandServiceGrpcKt.ConcessionStandServiceCoroutineStub(channel)),
        RelayDriverInboxesStatusDocument(DocumentServiceGrpcKt.DocumentServiceCoroutineStub(channel)),
    )) {
        sink.deliver(rec)
    }
}

/** Where relayDriverInboxesStatus hands each record. */
interface RelayDriverInboxesStatusSink {
    suspend fun deliver(rec: RelayDriverInboxesStatusRecord)
}

/** What relayDriverInboxesStatus keeps of each CountDriverInboxes response. */
data class RelayDriverInboxesStatusRecord(val status: String, val correlationId: String, val message: String)

/** Passes records to acme.sponsorships.v1.SponsorQueryService. */
class RelayDriverInboxesStatusSponsorQuery(private val stub: SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub) : RelayDriverInboxesStatusSink {
    override suspend fun deliver(rec: RelayDriverInboxesStatusRecord) {
        stub.summarizeSponsors(summarizeSponsorsRequest { this.reason = rec.correlationId })
    }
}

/** Passes records to acme.concessions.v1.ConcessionStandService. */
class RelayDriverInboxesStatusConcessionStand(private val stub: ConcessionStandServiceGrpcKt.ConcessionStandServiceCoroutineStub) : RelayDriverInboxesStatusSink {
    override suspend fun deliver(rec: RelayDriverInboxesStatusRecord) {
        stub.searchConcessionStands(searchConcessionStandsRequest { this.page = pageRequest { this.pageToken = rec.message } })
    }
}

/** Passes records to acme.documents.v1.DocumentService. */
class RelayDriverInboxesStatusDocument(private val stub: DocumentServiceGrpcKt.DocumentServiceCoroutineStub) : RelayDriverInboxesStatusSink {
    override suspend fun deliver(rec: RelayDriverInboxesStatusRecord) {
        stub.createDocument(createDocumentRequest { this.requestId = rec.message })
    }
}
