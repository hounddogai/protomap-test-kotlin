package com.acme.transport.terminal.dwell.exception

import com.acme.transport.terminal.sortation.sortationvariance.SeasonalSortationVariance
import com.acme.transport.terminal.sortation.sortationvariance.batchedSortationOverrideWeight

fun summarizeSeasonalSortationVariancesForAllocation(items: MutableList<SeasonalSortationVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveDwellAllocationsFromVariance(items: MutableList<SeasonalSortationVariance>): MutableList<DwellAllocation> {
    val out = mutableListOf<DwellAllocation>()
    for (item in items) {
        val derived = newDwellAllocation(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleDwellAllocation(item: DwellAllocation): Double = batchedSortationOverrideWeight(6, true)
