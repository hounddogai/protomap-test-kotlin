package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.terminal.crossdock.segment.ScheduledCrossDockRecord
import com.acme.transport.terminal.crossdock.segment.crossDockAllocationBucket

fun summarizeScheduledCrossDockRecordsForRollup(items: MutableList<ScheduledCrossDockRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveAdaptiveTrailerYardRollupsFromRecord(items: MutableList<ScheduledCrossDockRecord>): MutableList<AdaptiveTrailerYardRollup> {
    val out = mutableListOf<AdaptiveTrailerYardRollup>()
    for (item in items) {
        val derived = newAdaptiveTrailerYardRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchAdaptiveTrailerYardRollup(item: AdaptiveTrailerYardRollup): String {
    return crossDockAllocationBucket(item.weightedLoad())
}
