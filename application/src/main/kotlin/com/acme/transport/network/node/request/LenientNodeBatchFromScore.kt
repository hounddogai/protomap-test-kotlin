package com.acme.transport.network.node.request

import com.acme.transport.network.cutoff.preference.PendingCutoffScore
import com.acme.transport.network.cutoff.preference.pendingCutoffScoreBucket

fun summarizePendingCutoffScoresForBatch(items: MutableList<PendingCutoffScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveLenientNodeBatchesFromScore(items: MutableList<PendingCutoffScore>): MutableList<LenientNodeBatch> {
    val out = mutableListOf<LenientNodeBatch>()
    for (item in items) {
        val derived = newLenientNodeBatch(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyLenientNodeBatch(item: LenientNodeBatch): String = pendingCutoffScoreBucket(item.measuredValue())
