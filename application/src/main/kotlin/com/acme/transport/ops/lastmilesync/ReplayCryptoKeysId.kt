package com.acme.transport.ops.lastmilesync

import com.acme.budgeting.v1.BudgetCalculationServiceGrpcKt
import com.acme.budgeting.v1.CalculateBudgetRequestKt
import com.acme.budgeting.v1.calculateBudgetRequest
import com.acme.keymanagement.v1.CryptoKeyServiceGrpcKt
import com.acme.keymanagement.v1.searchCryptoKeysRequest
import com.acme.telematics.v1.GeoFenceSyncServiceGrpcKt
import com.acme.telematics.v1.PushGeoFenceRequestKt
import com.acme.telematics.v1.pushGeoFenceRequest
import com.acme.ticketing.v1.TicketServiceGrpcKt
import com.acme.ticketing.v1.getTicketRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.keymanagement.v1.CryptoKeyService/SearchCryptoKeys and hands what it keeps to the sink of its kind. */
suspend fun replayCryptoKeysId(channel: ManagedChannel) {
    val source = CryptoKeyServiceGrpcKt.CryptoKeyServiceCoroutineStub(channel)
    val current = RpcBoundary.read { source.searchCryptoKeys(searchCryptoKeysRequest { this.orderBy = "nightly-replay-crypto-keys-id" }) }
    val record = ReplayCryptoKeysIdRecord(
        correlationId = current.result.correlationId,
        status = current.result.status,
    )
    actOnReplayCryptoKeysId("budget-calculation", record, channel)
}

/** Where replayCryptoKeysId hands each record. */
interface ReplayCryptoKeysIdDestination {
    suspend fun write(rec: ReplayCryptoKeysIdRecord)
}

/** What replayCryptoKeysId keeps of each SearchCryptoKeys response. */
data class ReplayCryptoKeysIdRecord(val correlationId: String, val status: String)

/** Hands records to acme.telematics.v1.GeoFenceSyncService. */
class ReplayCryptoKeysIdGeoFenceSync(private val stub: GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub) : ReplayCryptoKeysIdDestination {
    override suspend fun write(rec: ReplayCryptoKeysIdRecord) {
        val request = pushGeoFenceRequest {
            this.details = PushGeoFenceRequestKt.details { this.resolution = rec.correlationId }
        }
        RpcBoundary.run("acme.telematics.v1.GeoFenceSyncService/PushGeoFence") { stub.pushGeoFence(request) }
    }
}

/** Sends records to acme.budgeting.v1.BudgetCalculationService. */
class ReplayCryptoKeysIdBudgetCalculation(private val stub: BudgetCalculationServiceGrpcKt.BudgetCalculationServiceCoroutineStub) : ReplayCryptoKeysIdDestination {
    override suspend fun write(rec: ReplayCryptoKeysIdRecord) {
        val request = calculateBudgetRequest {
            this.details = CalculateBudgetRequestKt.details { this.currencyCode = rec.correlationId }
        }
        stub.calculateBudget(request)
    }
}

/** Passes records to acme.ticketing.v1.TicketService. */
class ReplayCryptoKeysIdTicket(private val stub: TicketServiceGrpcKt.TicketServiceCoroutineStub) : ReplayCryptoKeysIdDestination {
    override suspend fun write(rec: ReplayCryptoKeysIdRecord) {
        RpcBoundary.run("acme.ticketing.v1.TicketService/GetTicket") { stub.getTicket(getTicketRequest { this.ticketId = rec.status }) }
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnReplayCryptoKeysId(kind: String, rec: ReplayCryptoKeysIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "geo-fence-sync" to { ReplayCryptoKeysIdGeoFenceSync(GeoFenceSyncServiceGrpcKt.GeoFenceSyncServiceCoroutineStub(channel)).write(rec) },
        "budget-calculation" to { ReplayCryptoKeysIdBudgetCalculation(BudgetCalculationServiceGrpcKt.BudgetCalculationServiceCoroutineStub(channel)).write(rec) },
        "ticket" to { ReplayCryptoKeysIdTicket(TicketServiceGrpcKt.TicketServiceCoroutineStub(channel)).write(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
