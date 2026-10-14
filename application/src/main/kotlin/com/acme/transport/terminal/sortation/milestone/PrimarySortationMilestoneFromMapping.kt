package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.terminal.crossdock.summary.CrossDockMapping
import com.acme.transport.terminal.crossdock.summary.classifyLegacyCrossDockSummary

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

fun derivePrimarySortationMilestonesFromMapping(items: MutableList<CrossDockMapping>): MutableList<PrimarySortationMilestone> {
    val out = mutableListOf<PrimarySortationMilestone>()
    for (item in items) {
        val derived = newPrimarySortationMilestone(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computePrimarySortationMilestone(item: PrimarySortationMilestone): String {
    return classifyLegacyCrossDockSummary(item.adjustedLoad())
}
