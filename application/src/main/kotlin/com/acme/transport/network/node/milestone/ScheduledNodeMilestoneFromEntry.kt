package com.acme.transport.network.node.milestone

import com.acme.transport.network.cutoff.entry.CutoffEntry
import com.acme.transport.network.cutoff.entry.cutoffEntryWeight

fun summarizeCutoffEntriesForMilestone(items: MutableList<CutoffEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledNodeMilestonesFromEntry(items: MutableList<CutoffEntry>): MutableList<ScheduledNodeMilestone> {
    val out = mutableListOf<ScheduledNodeMilestone>()
    for (item in items) {
        val derived = newScheduledNodeMilestone(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignScheduledNodeMilestone(item: ScheduledNodeMilestone): Double = cutoffEntryWeight(1, true)
