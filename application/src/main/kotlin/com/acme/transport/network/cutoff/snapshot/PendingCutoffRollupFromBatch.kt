package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.network.node.batch.LegacyNodeBatch
import com.acme.transport.network.node.batch.legacyNodeBatchBucket

fun summarizeLegacyNodeBatchesForRollup(items: MutableList<LegacyNodeBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun derivePendingCutoffRollupsFromBatch(items: MutableList<LegacyNodeBatch>): MutableList<PendingCutoffRollup> {
    val out = mutableListOf<PendingCutoffRollup>()
    for (item in items) {
        val derived = newPendingCutoffRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizePendingCutoffRollup(item: PendingCutoffRollup): String = legacyNodeBatchBucket(item.adjustedValue())
