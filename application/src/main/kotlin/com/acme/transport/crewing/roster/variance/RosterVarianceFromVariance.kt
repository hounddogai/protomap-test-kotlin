package com.acme.transport.crewing.roster.variance

import com.acme.transport.crewing.swap.variance.SwapVariance
import com.acme.transport.crewing.swap.variance.classifySwapException

fun summarizeSwapVariancesForVariance(items: MutableList<SwapVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveRosterVariancesFromVariance(items: MutableList<SwapVariance>): MutableList<RosterVariance> {
    val out = mutableListOf<RosterVariance>()
    for (item in items) {
        val derived = newRosterVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeRosterVariance(item: RosterVariance): String = classifySwapException(item.netVolume())
