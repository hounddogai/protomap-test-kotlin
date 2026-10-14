package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.network.transittime.bundle.SeasonalTransitTimeCheckpoint
import com.acme.transport.network.transittime.bundle.normalizeTieredTransitTimeBudgetCode

fun summarizeSeasonalTransitTimeCheckpointsForRollup(items: MutableList<SeasonalTransitTimeCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun derivePendingCutoffRollupsFromCheckpoint(items: MutableList<SeasonalTransitTimeCheckpoint>): MutableList<PendingCutoffRollup> {
    val out = mutableListOf<PendingCutoffRollup>()
    for (item in items) {
        val derived = newPendingCutoffRollup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocatePendingCutoffRollup(item: PendingCutoffRollup): String {
    return normalizeTieredTransitTimeBudgetCode(item.code)
}
