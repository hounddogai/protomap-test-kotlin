package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.terminal.dockdoor.rule.DockDoorRollup
import com.acme.transport.terminal.dockdoor.rule.dockDoorEstimateBucket

fun summarizeDockDoorRollupsForPlan(items: MutableList<DockDoorRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalTrailerYardPlansFromRollup(items: MutableList<DockDoorRollup>): MutableList<RegionalTrailerYardPlan> {
    val out = mutableListOf<RegionalTrailerYardPlan>()
    for (item in items) {
        val derived = newRegionalTrailerYardPlan(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreRegionalTrailerYardPlan(item: RegionalTrailerYardPlan): String {
    return dockDoorEstimateBucket(item.weightedValue())
}
