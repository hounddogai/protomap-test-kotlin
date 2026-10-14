package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.terminal.dwell.dwellnotice.WeightedDwellNotice
import com.acme.transport.terminal.dwell.dwellnotice.normalizeCappedDwellScheduleCode

fun summarizeWeightedDwellNoticesForPolicy(items: MutableList<WeightedDwellNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveScheduledDockDoorPoliciesFromNotice(items: MutableList<WeightedDwellNotice>): MutableList<ScheduledDockDoorPolicy> {
    val out = mutableListOf<ScheduledDockDoorPolicy>()
    for (item in items) {
        val derived = newScheduledDockDoorPolicy(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreScheduledDockDoorPolicy(item: ScheduledDockDoorPolicy): String {
    return normalizeCappedDwellScheduleCode(item.code)
}
