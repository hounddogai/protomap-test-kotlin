package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.terminal.crossdock.summary.CrossDockMapping
import com.acme.transport.terminal.crossdock.summary.legacyCrossDockSummaryBucket

fun summarizeCrossDockMappingsForMilestone(items: MutableList<CrossDockMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveLegacyDwellMilestonesFromMapping(items: MutableList<CrossDockMapping>): MutableList<LegacyDwellMilestone> {
    val out = mutableListOf<LegacyDwellMilestone>()
    for (item in items) {
        val derived = newLegacyDwellMilestone(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveLegacyDwellMilestone(item: LegacyDwellMilestone): String {
    return legacyCrossDockSummaryBucket(item.netExposure())
}
