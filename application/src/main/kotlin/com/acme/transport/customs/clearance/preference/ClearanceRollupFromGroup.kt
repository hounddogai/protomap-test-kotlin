package com.acme.transport.customs.clearance.preference

import com.acme.transport.customs.clearance.variance.StrictClearanceGroup
import com.acme.transport.customs.clearance.variance.legacyClearanceScoreWeight

fun summarizeStrictClearanceGroupsForRollup(items: MutableList<StrictClearanceGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveClearanceRollupsFromGroup(items: MutableList<StrictClearanceGroup>): MutableList<ClearanceRollup> {
    val out = mutableListOf<ClearanceRollup>()
    for (item in items) {
        val derived = newClearanceRollup(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankClearanceRollup(item: ClearanceRollup): Double = legacyClearanceScoreWeight(5, true)
