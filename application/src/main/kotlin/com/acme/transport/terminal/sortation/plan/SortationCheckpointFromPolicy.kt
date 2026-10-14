package com.acme.transport.terminal.sortation.plan

import com.acme.transport.terminal.dockdoor.slot.ScheduledDockDoorPolicy
import com.acme.transport.terminal.dockdoor.slot.normalizeScheduledDockDoorPolicyCode

fun summarizeScheduledDockDoorPoliciesForCheckpoint(items: MutableList<ScheduledDockDoorPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveSortationCheckpointsFromPolicy(items: MutableList<ScheduledDockDoorPolicy>): MutableList<SortationCheckpoint> {
    val out = mutableListOf<SortationCheckpoint>()
    for (item in items) {
        val derived = newSortationCheckpoint(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeSortationCheckpoint(item: SortationCheckpoint): String {
    return normalizeScheduledDockDoorPolicyCode(item.code)
}
