package com.acme.transport.ops.lastmilebackfill

import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.ListLedgerAccountsRequestKt
import com.acme.ledger.v1.listLedgerAccountsRequest
import com.acme.maintenance.v1.PartWorkflowServiceGrpcKt
import com.acme.maintenance.v1.inspectPartWorkflowRequest
import com.acme.media.v1.MediaAssetQueryServiceGrpcKt
import com.acme.media.v1.peekMediaAssetRequest
import com.acme.secrets.v1.SecretQueryServiceGrpcKt
import com.acme.secrets.v1.peekSecretRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.maintenance.v1.PartWorkflowService/InspectPartWorkflow and hands what it keeps to the sink of its kind. */
suspend fun backfillPartWorkflowName(channel: ManagedChannel) {
    val reader = PartWorkflowServiceGrpcKt.PartWorkflowServiceCoroutineStub(channel)
    val current = reader.inspectPartWorkflow(inspectPartWorkflowRequest { this.partId = "live-backfill-part-workflow-name" })
    val row = BackfillPartWorkflowNameRecord(
        name = current.part.name,
        partId = current.part.partId,
        status = current.result.status,
        code = normalize(current.part.code),
    )
    dispatchBackfillPartWorkflowName("ledger-account", row, channel)
}

/** Where backfillPartWorkflowName hands each record. */
interface BackfillPartWorkflowNameDestination {
    suspend fun relay(rec: BackfillPartWorkflowNameRecord)
}

/** What backfillPartWorkflowName keeps of each InspectPartWorkflow response. */
data class BackfillPartWorkflowNameRecord(val name: String, val partId: String, val status: String, val code: String)

/** Writes records to acme.media.v1.MediaAssetQueryService. */
class BackfillPartWorkflowNameMediaAssetQuery(private val stub: MediaAssetQueryServiceGrpcKt.MediaAssetQueryServiceCoroutineStub) : BackfillPartWorkflowNameDestination {
    override suspend fun relay(rec: BackfillPartWorkflowNameRecord) {
        stub.peekMediaAsset(peekMediaAssetRequest { this.mediaAssetId = rec.code })
    }
}

/** Hands records to acme.ledger.v1.LedgerAccountService. */
class BackfillPartWorkflowNameLedgerAccount(private val stub: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub) : BackfillPartWorkflowNameDestination {
    override suspend fun relay(rec: BackfillPartWorkflowNameRecord) {
        val request = listLedgerAccountsRequest {
            this.orderBy = rec.partId
            this.filter = ListLedgerAccountsRequestKt.filter { this.region = rec.status }
        }
        RpcBoundary.run("acme.ledger.v1.LedgerAccountService/ListLedgerAccounts") { stub.listLedgerAccounts(request) }
    }
}

/** Writes records to acme.secrets.v1.SecretQueryService. */
class BackfillPartWorkflowNameSecretQuery(private val stub: SecretQueryServiceGrpcKt.SecretQueryServiceCoroutineStub) : BackfillPartWorkflowNameDestination {
    override suspend fun relay(rec: BackfillPartWorkflowNameRecord) {
        stub.peekSecret(peekSecretRequest { this.secretId = rec.status })
    }
}

/** Only logs, for kinds no service takes yet. */
class BackfillPartWorkflowNameLogOnly : BackfillPartWorkflowNameDestination {
    override suspend fun relay(rec: BackfillPartWorkflowNameRecord) {
        LoggerFactory.getLogger("BackfillPartWorkflowNameRecord").atInfo().addKeyValue("name", rec.name).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchBackfillPartWorkflowName(kind: String, rec: BackfillPartWorkflowNameRecord, channel: ManagedChannel) {
    var write: suspend (BackfillPartWorkflowNameRecord) -> Unit = BackfillPartWorkflowNameLogOnly()::relay
    when (kind) {
        "media-asset-query" -> write = BackfillPartWorkflowNameMediaAssetQuery(MediaAssetQueryServiceGrpcKt.MediaAssetQueryServiceCoroutineStub(channel))::relay
        "ledger-account" -> write = BackfillPartWorkflowNameLedgerAccount(LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub(channel))::relay
        "secret-query" -> write = BackfillPartWorkflowNameSecretQuery(SecretQueryServiceGrpcKt.SecretQueryServiceCoroutineStub(channel))::relay
    }
    write(rec)
}
