package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.linehaul.trailer.trailergroup.RollingTrailerGroup
import com.acme.transport.linehaul.trailer.trailergroup.normalizeRollingTrailerGroupCode

fun summarizeRollingTrailerGroupsForRollup(items: MutableList<RollingTrailerGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientCapacityRollupsFromGroup(items: MutableList<RollingTrailerGroup>): MutableList<LenientCapacityRollup> {
    val out = mutableListOf<LenientCapacityRollup>()
    for (item in items) {
        val derived = newLenientCapacityRollup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchLenientCapacityRollup(item: LenientCapacityRollup): String = normalizeRollingTrailerGroupCode(item.code)
