package com.acme.transport.terminal.dwell.assignment

import com.acme.transport.terminal.crossdock.dockproposal.GlobalCrossDockVariance
import com.acme.transport.terminal.crossdock.dockproposal.classifyPrimaryCrossDockMilestone

fun summarizeGlobalCrossDockVariancesForPreference(items: MutableList<GlobalCrossDockVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackDwellPreferencesFromVariance(items: MutableList<GlobalCrossDockVariance>): MutableList<FallbackDwellPreference> {
    val out = mutableListOf<FallbackDwellPreference>()
    for (item in items) {
        val derived = newFallbackDwellPreference(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyFallbackDwellPreference(item: FallbackDwellPreference): String {
    return classifyPrimaryCrossDockMilestone(item.projectedTotal())
}
