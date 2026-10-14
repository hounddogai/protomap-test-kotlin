package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.emissions.fuelburn.allocation.RollingFuelBurnRecord
import com.acme.transport.emissions.fuelburn.allocation.classifyRollingFuelBurnRecord

fun summarizeRollingFuelBurnRecordsForNotice(items: MutableList<RollingFuelBurnRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveSeasonalRouteOffsetNoticesFromRecord(items: MutableList<RollingFuelBurnRecord>): MutableList<SeasonalRouteOffsetNotice> {
    val out = mutableListOf<SeasonalRouteOffsetNotice>()
    for (item in items) {
        val derived = newSeasonalRouteOffsetNotice(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeSeasonalRouteOffsetNotice(item: SeasonalRouteOffsetNotice): String {
    return classifyRollingFuelBurnRecord(item.effectiveVolume())
}
