package com.acme.transport.ops.networkmirror

import com.acme.common.v1.pageRequest
import com.acme.fleet.v1.ExpireInspectionRequestKt
import com.acme.fleet.v1.InspectionLifecycleServiceGrpcKt
import com.acme.fleet.v1.expireInspectionRequest
import com.acme.fuel.v1.FuelTransactionQueryServiceGrpcKt
import com.acme.fuel.v1.findFuelTransactionsRequest
import com.acme.keymanagement.v1.CryptoKeyQueryServiceGrpcKt
import com.acme.keymanagement.v1.browseCryptoKeysRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.keymanagement.v1.CryptoKeyQueryService/BrowseCryptoKeys and hands what it keeps to the sink of its kind. */
suspend fun exportCryptoKeysMessage(channel: ManagedChannel) {
    val reader = CryptoKeyQueryServiceGrpcKt.CryptoKeyQueryServiceCoroutineStub(channel)
    val current = reader.browseCryptoKeys(browseCryptoKeysRequest { this.orderBy = "current-export-crypto-keys-message" })
    val row = ExportCryptoKeysMessageRecord(
        message = current.result.message,
        status = normalize(current.result.status),
        correlationId = current.result.correlationId,
        nextPageToken = current.pageInfo.nextPageToken,
    )
    val sinks = mapOf<String, ExportCryptoKeysMessageTarget>(
        "fuel-transaction-query" to ExportCryptoKeysMessageFuelTransactionQuery(FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub(channel)),
        "inspection-lifecycle" to ExportCryptoKeysMessageInspectionLifecycle(InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub(channel)),
    )
    routeExportCryptoKeysMessage(sinks, "inspection-lifecycle", row)
}

/** Where exportCryptoKeysMessage hands each record. */
interface ExportCryptoKeysMessageTarget {
    suspend fun forward(rec: ExportCryptoKeysMessageRecord)
}

/** What exportCryptoKeysMessage keeps of each BrowseCryptoKeys response. */
data class ExportCryptoKeysMessageRecord(val message: String, val status: String, val correlationId: String, val nextPageToken: String)

/** Sends records to acme.fuel.v1.FuelTransactionQueryService. */
class ExportCryptoKeysMessageFuelTransactionQuery(private val stub: FuelTransactionQueryServiceGrpcKt.FuelTransactionQueryServiceCoroutineStub) : ExportCryptoKeysMessageTarget {
    override suspend fun forward(rec: ExportCryptoKeysMessageRecord) {
        val request = findFuelTransactionsRequest {
            this.page = pageRequest { this.pageToken = rec.status }
            this.orderBy = rec.nextPageToken
        }
        RpcBoundary.run("acme.fuel.v1.FuelTransactionQueryService/FindFuelTransactions") { stub.findFuelTransactions(request) }
    }
}

/** Hands records to acme.fleet.v1.InspectionLifecycleService. */
class ExportCryptoKeysMessageInspectionLifecycle(private val stub: InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub) : ExportCryptoKeysMessageTarget {
    override suspend fun forward(rec: ExportCryptoKeysMessageRecord) {
        val request = expireInspectionRequest {
            this.details = ExpireInspectionRequestKt.details { this.reasonCode = rec.correlationId }
        }
        stub.expireInspection(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeExportCryptoKeysMessage(sinks: Map<String, ExportCryptoKeysMessageTarget>, kind: String, rec: ExportCryptoKeysMessageRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.forward(rec)
}
