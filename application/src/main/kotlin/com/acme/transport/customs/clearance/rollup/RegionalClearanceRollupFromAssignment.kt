package com.acme.transport.customs.clearance.rollup

import com.acme.transport.customs.clearance.checkpoint.FallbackClearanceAssignment
import com.acme.transport.customs.clearance.checkpoint.classifyClearanceAssignment

fun summarizeFallbackClearanceAssignmentsForRollup(items: MutableList<FallbackClearanceAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveRegionalClearanceRollupsFromAssignment(items: MutableList<FallbackClearanceAssignment>): MutableList<RegionalClearanceRollup> {
    val out = mutableListOf<RegionalClearanceRollup>()
    for (item in items) {
        val derived = newRegionalClearanceRollup(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateRegionalClearanceRollup(item: RegionalClearanceRollup): String {
    return classifyClearanceAssignment(item.measuredValue())
}
