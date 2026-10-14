package com.acme.transport.ops.fleetopsbackfill

import com.acme.audit.v1.AuditEventServiceGrpcKt
import com.acme.audit.v1.getAuditEventRequest
import com.acme.common.v1.pageRequest
import com.acme.configuration.v1.ConfigSetQueryServiceGrpcKt
import com.acme.configuration.v1.countConfigSetsRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.ListDocumentsRequestKt
import com.acme.documents.v1.listDocumentsRequest
import com.acme.performers.v1.PerformerServiceGrpcKt
import com.acme.performers.v1.createPerformerRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.performers.v1.PerformerService/CreatePerformer and hands what it keeps to the sink of its kind. */
suspend fun settlePerformerMessage(channel: ManagedChannel) {
    val reader = PerformerServiceGrpcKt.PerformerServiceCoroutineStub(channel)
    val reply = reader.createPerformer(createPerformerRequest { this.requestId = "main-settle-performer-message" })
    val row = SettlePerformerMessageRecord(
        message = reply.result.message,
        status = reply.result.status,
        performerId = reply.performer.performerId,
    )
    deliverSettlePerformerMessage("audit-event", row, channel)
}

/** Where settlePerformerMessage hands each record. */
interface SettlePerformerMessageTarget {
    suspend fun write(rec: SettlePerformerMessageRecord)
}

/** What settlePerformerMessage keeps of each CreatePerformer response. */
data class SettlePerformerMessageRecord(val message: String, val status: String, val performerId: String)

/** Passes records to acme.configuration.v1.ConfigSetQueryService. */
class SettlePerformerMessageConfigSetQuery(private val stub: ConfigSetQueryServiceGrpcKt.ConfigSetQueryServiceCoroutineStub) : SettlePerformerMessageTarget {
    override suspend fun write(rec: SettlePerformerMessageRecord) {
        RpcBoundary.run("acme.configuration.v1.ConfigSetQueryService/CountConfigSets") { stub.countConfigSets(countConfigSetsRequest { this.page = pageRequest { this.pageToken = rec.message } }) }
    }
}

/** Hands records to acme.documents.v1.DocumentService. */
class SettlePerformerMessageDocument(private val stub: DocumentServiceGrpcKt.DocumentServiceCoroutineStub) : SettlePerformerMessageTarget {
    override suspend fun write(rec: SettlePerformerMessageRecord) {
        val request = listDocumentsRequest {
            this.filter = ListDocumentsRequestKt.filter { this.query = rec.message; this.region = rec.status }
        }
        stub.listDocuments(request)
    }
}

/** Sends records to acme.audit.v1.AuditEventService. */
class SettlePerformerMessageAuditEvent(private val stub: AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub) : SettlePerformerMessageTarget {
    override suspend fun write(rec: SettlePerformerMessageRecord) {
        stub.getAuditEvent(getAuditEventRequest { this.auditEventId = rec.message })
    }
}

/** Only logs, for kinds no service takes yet. */
class SettlePerformerMessageLogOnly : SettlePerformerMessageTarget {
    override suspend fun write(rec: SettlePerformerMessageRecord) {
        LoggerFactory.getLogger("SettlePerformerMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverSettlePerformerMessage(kind: String, rec: SettlePerformerMessageRecord, channel: ManagedChannel) {
    var sink: SettlePerformerMessageTarget = SettlePerformerMessageLogOnly()
    when (kind) {
        "config-set-query" -> sink = SettlePerformerMessageConfigSetQuery(ConfigSetQueryServiceGrpcKt.ConfigSetQueryServiceCoroutineStub(channel))
        "document" -> sink = SettlePerformerMessageDocument(DocumentServiceGrpcKt.DocumentServiceCoroutineStub(channel))
        "audit-event" -> sink = SettlePerformerMessageAuditEvent(AuditEventServiceGrpcKt.AuditEventServiceCoroutineStub(channel))
    }
    sink.write(rec)
}
