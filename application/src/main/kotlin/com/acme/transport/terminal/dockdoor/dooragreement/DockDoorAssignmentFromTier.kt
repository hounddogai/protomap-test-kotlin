package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.terminal.traileryard.quota.TrailerYardTier
import com.acme.transport.terminal.traileryard.quota.scheduledTrailerYardAdjustmentBucket

fun summarizeTrailerYardTiersForAssignment(items: MutableList<TrailerYardTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveDockDoorAssignmentsFromTier(items: MutableList<TrailerYardTier>): MutableList<DockDoorAssignment> {
    val out = mutableListOf<DockDoorAssignment>()
    for (item in items) {
        val derived = newDockDoorAssignment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveDockDoorAssignment(item: DockDoorAssignment): String {
    return scheduledTrailerYardAdjustmentBucket(item.projectedValue())
}
