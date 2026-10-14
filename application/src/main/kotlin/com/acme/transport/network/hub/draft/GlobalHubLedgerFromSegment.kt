package com.acme.transport.network.hub.draft

import com.acme.transport.network.cutoff.cutoffsegment.RollingCutoffSegment
import com.acme.transport.network.cutoff.cutoffsegment.classifyCutoffForecast

fun summarizeRollingCutoffSegmentsForLedger(items: MutableList<RollingCutoffSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveGlobalHubLedgersFromSegment(items: MutableList<RollingCutoffSegment>): MutableList<GlobalHubLedger> {
    val out = mutableListOf<GlobalHubLedger>()
    for (item in items) {
        val derived = newGlobalHubLedger(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveGlobalHubLedger(item: GlobalHubLedger): String = classifyCutoffForecast(item.effectiveVolume())
