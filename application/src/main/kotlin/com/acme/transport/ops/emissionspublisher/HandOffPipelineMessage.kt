package com.acme.transport.ops.emissionspublisher

import com.acme.customers.v1.ConsentRecordSyncServiceGrpcKt
import com.acme.customers.v1.PullConsentRecordsRequestKt
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.pullConsentRecordsRequest
import com.acme.fuel.v1.FuelTransactionApprovalServiceGrpcKt
import com.acme.fuel.v1.ListPendingFuelTransactionsRequestKt
import com.acme.fuel.v1.listPendingFuelTransactionsRequest
import com.acme.keymanagement.v1.CryptoKeyQueryServiceGrpcKt
import com.acme.keymanagement.v1.lookupCryptoKeyRequest
import com.acme.pipelines.v1.PipelineCalculationServiceGrpcKt
import com.acme.pipelines.v1.calculatePipelineRequest
import io.grpc.ManagedChannel

/** Reads acme.pipelines.v1.PipelineCalculationService/CalculatePipeline and hands what it keeps to the sink of its kind. */
suspend fun handOffPipelineMessage(channel: ManagedChannel) {
    val origin = PipelineCalculationServiceGrpcKt.PipelineCalculationServiceCoroutineStub(channel)
    val got = origin.calculatePipeline(calculatePipelineRequest { this.pipelineId = "default-hand-off-pipeline-message" })
    val entry = HandOffPipelineMessageRecord(
        message = got.result.message,
        pipelineId = got.pipeline.pipelineId,
        currencyCode = got.breakdown.currencyCode,
    )
    val sinks = mapOf<String, HandOffPipelineMessageWriter>(
        "crypto-key-query" to HandOffPipelineMessageCryptoKeyQuery(CryptoKeyQueryServiceGrpcKt.CryptoKeyQueryServiceCoroutineStub(channel)),
        "fuel-transaction-approval" to HandOffPipelineMessageFuelTransactionApproval(FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub(channel)),
        "consent-record-sync" to HandOffPipelineMessageConsentRecordSync(ConsentRecordSyncServiceGrpcKt.ConsentRecordSyncServiceCoroutineStub(channel)),
    )
    routeHandOffPipelineMessage(sinks, "crypto-key-query", entry)
}

/** Where handOffPipelineMessage hands each record. */
interface HandOffPipelineMessageWriter {
    suspend fun deliver(rec: HandOffPipelineMessageRecord)
}

/** What handOffPipelineMessage keeps of each CalculatePipeline response. */
data class HandOffPipelineMessageRecord(val message: String, val pipelineId: String, val currencyCode: String)

/** Passes records to acme.keymanagement.v1.CryptoKeyQueryService. */
class HandOffPipelineMessageCryptoKeyQuery(private val stub: CryptoKeyQueryServiceGrpcKt.CryptoKeyQueryServiceCoroutineStub) : HandOffPipelineMessageWriter {
    override suspend fun deliver(rec: HandOffPipelineMessageRecord) {
        stub.lookupCryptoKey(lookupCryptoKeyRequest { this.cryptoKeyId = rec.currencyCode })
    }
}

/** Hands records to acme.fuel.v1.FuelTransactionApprovalService. */
class HandOffPipelineMessageFuelTransactionApproval(private val stub: FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub) : HandOffPipelineMessageWriter {
    override suspend fun deliver(rec: HandOffPipelineMessageRecord) {
        val request = listPendingFuelTransactionsRequest {
            this.orderBy = rec.currencyCode
            this.filter = ListPendingFuelTransactionsRequestKt.filter { this.ownerId = rec.pipelineId; this.region = rec.message }
        }
        stub.listPendingFuelTransactions(request)
    }
}

/** Sends records to acme.customers.v1.ConsentRecordSyncService. */
class HandOffPipelineMessageConsentRecordSync(private val stub: ConsentRecordSyncServiceGrpcKt.ConsentRecordSyncServiceCoroutineStub) : HandOffPipelineMessageWriter {
    override suspend fun deliver(rec: HandOffPipelineMessageRecord) {
        val request = pullConsentRecordsRequest {
            this.consentRecord = consentRecord { this.source = rec.pipelineId; this.consentRecordId = rec.message }
            this.details = PullConsentRecordsRequestKt.details { this.peerRegion = rec.currencyCode }
        }
        stub.pullConsentRecords(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeHandOffPipelineMessage(sinks: Map<String, HandOffPipelineMessageWriter>, kind: String, rec: HandOffPipelineMessageRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.deliver(rec)
}
