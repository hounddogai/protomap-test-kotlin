package com.acme.transport.ops.crewingreconciler

import com.acme.catalog.v1.ProductCalculationServiceGrpcKt
import com.acme.catalog.v1.compareProductOptionsRequest
import com.acme.concessions.v1.ConcessionStandQueryServiceGrpcKt
import com.acme.concessions.v1.FindConcessionStandsRequestKt
import com.acme.concessions.v1.findConcessionStandsRequest
import com.acme.dispatch.v1.RecalculateZonesRequestKt
import com.acme.dispatch.v1.ZoneCalculationServiceGrpcKt
import com.acme.dispatch.v1.recalculateZonesRequest
import com.acme.dispatch.v1.zone
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.catalog.v1.ProductCalculationService/CompareProductOptions and hands what it keeps to the sink of its kind. */
suspend fun mirrorProductOptionsCode(channel: ManagedChannel) {
    val origin = ProductCalculationServiceGrpcKt.ProductCalculationServiceCoroutineStub(channel)
    val got = origin.compareProductOptions(compareProductOptionsRequest { this.productId = "nightly-mirror-product-options-code" })
    val rec = MirrorProductOptionsCodeRecord(
        code = got.product.code,
        currencyCode = got.breakdown.currencyCode,
        message = got.result.message,
    )
    actOnMirrorProductOptionsCode("concession-stand-query", rec, channel)
}

/** Where mirrorProductOptionsCode hands each record. */
interface MirrorProductOptionsCodeDestination {
    suspend fun hand(rec: MirrorProductOptionsCodeRecord)
}

/** What mirrorProductOptionsCode keeps of each CompareProductOptions response. */
data class MirrorProductOptionsCodeRecord(val code: String, val currencyCode: String, val message: String)

/** Writes records to acme.concessions.v1.ConcessionStandQueryService. */
class MirrorProductOptionsCodeConcessionStandQuery(private val stub: ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub) : MirrorProductOptionsCodeDestination {
    override suspend fun hand(rec: MirrorProductOptionsCodeRecord) {
        val request = findConcessionStandsRequest {
            this.orderBy = rec.message
            this.filter = FindConcessionStandsRequestKt.filter { this.region = rec.code }
        }
        RpcBoundary.run("acme.concessions.v1.ConcessionStandQueryService/FindConcessionStands") { stub.findConcessionStands(request) }
    }
}

/** Writes records to acme.dispatch.v1.ZoneCalculationService. */
class MirrorProductOptionsCodeZoneCalculation(private val stub: ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub) : MirrorProductOptionsCodeDestination {
    override suspend fun hand(rec: MirrorProductOptionsCodeRecord) {
        val request = recalculateZonesRequest {
            this.details = RecalculateZonesRequestKt.details { this.currencyCode = rec.currencyCode }
            this.reason = rec.code
            this.zone = zone { this.name = rec.message }
        }
        stub.recalculateZones(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnMirrorProductOptionsCode(kind: String, rec: MirrorProductOptionsCodeRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "concession-stand-query" to { MirrorProductOptionsCodeConcessionStandQuery(ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub(channel)).hand(rec) },
        "zone-calculation" to { MirrorProductOptionsCodeZoneCalculation(ZoneCalculationServiceGrpcKt.ZoneCalculationServiceCoroutineStub(channel)).hand(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
