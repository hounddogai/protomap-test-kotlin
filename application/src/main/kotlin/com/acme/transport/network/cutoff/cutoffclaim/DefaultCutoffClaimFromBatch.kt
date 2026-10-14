package com.acme.transport.network.cutoff.cutoffclaim

import com.acme.transport.network.hub.batch.HubBatch
import com.acme.transport.network.hub.batch.classifyHubEstimate

fun summarizeHubBatchesForClaim(items: MutableList<HubBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveDefaultCutoffClaimsFromBatch(items: MutableList<HubBatch>): MutableList<DefaultCutoffClaim> {
    val out = mutableListOf<DefaultCutoffClaim>()
    for (item in items) {
        val derived = newDefaultCutoffClaim(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileDefaultCutoffClaim(item: DefaultCutoffClaim): String = classifyHubEstimate(item.weightedTotal())
