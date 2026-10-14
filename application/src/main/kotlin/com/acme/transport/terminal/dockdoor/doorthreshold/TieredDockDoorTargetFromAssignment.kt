package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.terminal.dwell.assignment.PendingDwellAssignment
import com.acme.transport.terminal.dwell.assignment.classifyFallbackDwellPreference

fun summarizePendingDwellAssignmentsForTarget(items: MutableList<PendingDwellAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveTieredDockDoorTargetsFromAssignment(items: MutableList<PendingDwellAssignment>): MutableList<TieredDockDoorTarget> {
    val out = mutableListOf<TieredDockDoorTarget>()
    for (item in items) {
        val derived = newTieredDockDoorTarget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceTieredDockDoorTarget(item: TieredDockDoorTarget): String {
    return classifyFallbackDwellPreference(item.projectedTotal())
}
