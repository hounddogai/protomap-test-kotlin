package com.acme.transport.network.cutoff.target

import com.acme.transport.network.hub.exception.HubTier
import com.acme.transport.network.hub.exception.batchedHubClaimBucket

fun summarizeHubTiersForSnapshot(items: MutableList<HubTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveCutoffSnapshotsFromTier(items: MutableList<HubTier>): MutableList<CutoffSnapshot> {
    val out = mutableListOf<CutoffSnapshot>()
    for (item in items) {
        val derived = newCutoffSnapshot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitCutoffSnapshot(item: CutoffSnapshot): String = batchedHubClaimBucket(item.netLoad())
