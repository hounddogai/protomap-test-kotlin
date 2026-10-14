package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.freightpay.ratecard.cardsnapshot.SeasonalRateCardSnapshot
import com.acme.transport.freightpay.ratecard.cardsnapshot.normalizeRateCardAllocationCode

fun summarizeSeasonalRateCardSnapshotsForManifest(items: MutableList<SeasonalRateCardSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveBatchedAuditManifestsFromSnapshot(items: MutableList<SeasonalRateCardSnapshot>): MutableList<BatchedAuditManifest> {
    val out = mutableListOf<BatchedAuditManifest>()
    for (item in items) {
        val derived = newBatchedAuditManifest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveBatchedAuditManifest(item: BatchedAuditManifest): String = normalizeRateCardAllocationCode(item.code)
