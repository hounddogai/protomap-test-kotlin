package com.acme.transport.linehaul.capacity.checkpoint

import com.acme.transport.linehaul.capacity.capacityassignment.RegionalCapacityProfile
import com.acme.transport.linehaul.capacity.capacityassignment.classifyCapacityAdjustment

fun summarizeRegionalCapacityProfilesForCheckpoint(items: MutableList<RegionalCapacityProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveLenientCapacityCheckpointsFromProfile(items: MutableList<RegionalCapacityProfile>): MutableList<LenientCapacityCheckpoint> {
    val out = mutableListOf<LenientCapacityCheckpoint>()
    for (item in items) {
        val derived = newLenientCapacityCheckpoint(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitLenientCapacityCheckpoint(item: LenientCapacityCheckpoint): String {
    return classifyCapacityAdjustment(item.netValue())
}
