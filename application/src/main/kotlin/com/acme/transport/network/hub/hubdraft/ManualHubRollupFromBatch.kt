package com.acme.transport.network.hub.hubdraft

import com.acme.transport.network.cutoff.estimate.PrimaryCutoffBatch
import com.acme.transport.network.cutoff.estimate.cutoffReviewBucket

fun summarizePrimaryCutoffBatchesForRollup(items: MutableList<PrimaryCutoffBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveManualHubRollupsFromBatch(items: MutableList<PrimaryCutoffBatch>): MutableList<ManualHubRollup> {
    val out = mutableListOf<ManualHubRollup>()
    for (item in items) {
        val derived = newManualHubRollup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveManualHubRollup(item: ManualHubRollup): String = cutoffReviewBucket(item.netLoad())
