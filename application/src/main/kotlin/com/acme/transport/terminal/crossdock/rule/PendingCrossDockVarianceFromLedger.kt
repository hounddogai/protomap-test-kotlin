package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.terminal.sortation.forecast.RegionalSortationLedger
import com.acme.transport.terminal.sortation.forecast.classifyRegionalSortationLedger

fun summarizeRegionalSortationLedgersForVariance(items: MutableList<RegionalSortationLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun derivePendingCrossDockVariancesFromLedger(items: MutableList<RegionalSortationLedger>): MutableList<PendingCrossDockVariance> {
    val out = mutableListOf<PendingCrossDockVariance>()
    for (item in items) {
        val derived = newPendingCrossDockVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocatePendingCrossDockVariance(item: PendingCrossDockVariance): String {
    return classifyRegionalSortationLedger(item.effectiveVolume())
}
