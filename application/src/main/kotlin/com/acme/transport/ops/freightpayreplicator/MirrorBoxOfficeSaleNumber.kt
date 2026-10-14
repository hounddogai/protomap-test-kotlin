package com.acme.transport.ops.freightpayreplicator

import com.acme.boxoffice.v1.BoxOfficeSaleServiceGrpcKt
import com.acme.boxoffice.v1.cloneBoxOfficeSaleRequest
import com.acme.secrets.v1.SecretServiceGrpcKt
import com.acme.secrets.v1.deleteSecretRequest
import com.acme.telematics.v1.SyncTelemetryReadingsRequestKt
import com.acme.telematics.v1.TelemetryReadingSyncServiceGrpcKt
import com.acme.telematics.v1.syncTelemetryReadingsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.boxoffice.v1.BoxOfficeSaleService/CloneBoxOfficeSale and hands what it keeps to the sink of its kind. */
suspend fun mirrorBoxOfficeSaleNumber(channel: ManagedChannel) {
    val upstream = BoxOfficeSaleServiceGrpcKt.BoxOfficeSaleServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.cloneBoxOfficeSale(cloneBoxOfficeSaleRequest { this.requestId = "main-mirror-box-office-sale-number" }) }
    val rec = MirrorBoxOfficeSaleNumberRecord(
        number = reply.boxOfficeSale.number,
        status = normalize(reply.result.status),
        boxOfficeSaleId = reply.boxOfficeSale.boxOfficeSaleId,
    )
    deliverMirrorBoxOfficeSaleNumber("secret", rec, channel)
}

/** Where mirrorBoxOfficeSaleNumber hands each record. */
interface MirrorBoxOfficeSaleNumberTarget {
    suspend fun forward(rec: MirrorBoxOfficeSaleNumberRecord)
}

/** What mirrorBoxOfficeSaleNumber keeps of each CloneBoxOfficeSale response. */
data class MirrorBoxOfficeSaleNumberRecord(val number: String, val status: String, val boxOfficeSaleId: String)

/** Passes records to acme.secrets.v1.SecretService. */
class MirrorBoxOfficeSaleNumberSecret(private val stub: SecretServiceGrpcKt.SecretServiceCoroutineStub) : MirrorBoxOfficeSaleNumberTarget {
    override suspend fun forward(rec: MirrorBoxOfficeSaleNumberRecord) {
        val request = deleteSecretRequest {
            this.secretId = rec.status
            this.etag = rec.boxOfficeSaleId
            this.reason = rec.number
        }
        RpcBoundary.run("acme.secrets.v1.SecretService/DeleteSecret") { stub.deleteSecret(request) }
    }
}

/** Writes records to acme.telematics.v1.TelemetryReadingSyncService. */
class MirrorBoxOfficeSaleNumberTelemetryReadingSync(private val stub: TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub) : MirrorBoxOfficeSaleNumberTarget {
    override suspend fun forward(rec: MirrorBoxOfficeSaleNumberRecord) {
        val request = syncTelemetryReadingsRequest {
            this.details = SyncTelemetryReadingsRequestKt.details { this.peerRegion = rec.number; this.resolution = rec.status }
        }
        stub.syncTelemetryReadings(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class MirrorBoxOfficeSaleNumberLogOnly : MirrorBoxOfficeSaleNumberTarget {
    override suspend fun forward(rec: MirrorBoxOfficeSaleNumberRecord) {
        LoggerFactory.getLogger("MirrorBoxOfficeSaleNumberRecord").atInfo().addKeyValue("number", rec.number).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverMirrorBoxOfficeSaleNumber(kind: String, rec: MirrorBoxOfficeSaleNumberRecord, channel: ManagedChannel) {
    var sink: MirrorBoxOfficeSaleNumberTarget = MirrorBoxOfficeSaleNumberLogOnly()
    when (kind) {
        "secret" -> sink = MirrorBoxOfficeSaleNumberSecret(SecretServiceGrpcKt.SecretServiceCoroutineStub(channel))
        "telemetry-reading-sync" -> sink = MirrorBoxOfficeSaleNumberTelemetryReadingSync(TelemetryReadingSyncServiceGrpcKt.TelemetryReadingSyncServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
