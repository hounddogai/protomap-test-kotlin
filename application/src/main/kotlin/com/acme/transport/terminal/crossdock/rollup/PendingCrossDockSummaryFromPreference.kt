package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.terminal.dwell.assignment.FallbackDwellPreference
import com.acme.transport.terminal.dwell.assignment.fallbackDwellPreferenceWeight

fun summarizeFallbackDwellPreferencesForSummary(items: MutableList<FallbackDwellPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun derivePendingCrossDockSummariesFromPreference(items: MutableList<FallbackDwellPreference>): MutableList<PendingCrossDockSummary> {
    val out = mutableListOf<PendingCrossDockSummary>()
    for (item in items) {
        val derived = newPendingCrossDockSummary(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectPendingCrossDockSummary(item: PendingCrossDockSummary): Double = fallbackDwellPreferenceWeight(1, true)
