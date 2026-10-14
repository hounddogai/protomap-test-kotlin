package com.acme.transport.terminal.dockdoor.metric

import com.acme.transport.terminal.dwell.bundle.AdaptiveDwellEstimate
import com.acme.transport.terminal.dwell.bundle.classifyDwellOverride

fun summarizeAdaptiveDwellEstimatesForMilestone(items: MutableList<AdaptiveDwellEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveDockDoorMilestonesFromEstimate(items: MutableList<AdaptiveDwellEstimate>): MutableList<DockDoorMilestone> {
    val out = mutableListOf<DockDoorMilestone>()
    for (item in items) {
        val derived = newDockDoorMilestone(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeDockDoorMilestoneExtra(item: DockDoorMilestone): String = classifyDwellOverride(item.adjustedLoad())
