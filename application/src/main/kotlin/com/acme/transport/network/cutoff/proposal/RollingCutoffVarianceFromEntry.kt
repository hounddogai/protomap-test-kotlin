package com.acme.transport.network.cutoff.proposal

import com.acme.transport.network.transittime.scheduledtimebatch.DefaultTransitTimeEntry
import com.acme.transport.network.transittime.scheduledtimebatch.normalizeScheduledTransitTimeBatchCode

fun summarizeDefaultTransitTimeEntriesForVariance(items: MutableList<DefaultTransitTimeEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRollingCutoffVariancesFromEntry(items: MutableList<DefaultTransitTimeEntry>): MutableList<RollingCutoffVariance> {
    val out = mutableListOf<RollingCutoffVariance>()
    for (item in items) {
        val derived = newRollingCutoffVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitRollingCutoffVariance(item: RollingCutoffVariance): String {
    return normalizeScheduledTransitTimeBatchCode(item.code)
}
