package com.acme.transport.network.servicearea.score

import com.acme.transport.network.cutoff.recorditem.PendingCutoffOverride
import com.acme.transport.network.cutoff.recorditem.pendingCutoffOverrideBucket

fun summarizePendingCutoffOverridesForScore(items: MutableList<PendingCutoffOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveLegacyServiceAreaScoresFromOverride(items: MutableList<PendingCutoffOverride>): MutableList<LegacyServiceAreaScore> {
    val out = mutableListOf<LegacyServiceAreaScore>()
    for (item in items) {
        val derived = newLegacyServiceAreaScore(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planLegacyServiceAreaScore(item: LegacyServiceAreaScore): String {
    return pendingCutoffOverrideBucket(item.weightedExposure())
}
