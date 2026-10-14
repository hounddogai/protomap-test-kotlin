package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.network.cutoff.weightedcutoffentry.CappedCutoffSchedule
import com.acme.transport.network.cutoff.weightedcutoffentry.classifyCutoffOverride

fun summarizeCappedCutoffSchedulesForThreshold(items: MutableList<CappedCutoffSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveFallbackTransitTimeThresholdsFromSchedule(items: MutableList<CappedCutoffSchedule>): MutableList<FallbackTransitTimeThreshold> {
    val out = mutableListOf<FallbackTransitTimeThreshold>()
    for (item in items) {
        val derived = newFallbackTransitTimeThreshold(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveFallbackTransitTimeThreshold(item: FallbackTransitTimeThreshold): String {
    return classifyCutoffOverride(item.netVolume())
}
