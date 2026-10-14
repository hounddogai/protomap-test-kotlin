package com.acme.transport.customs.hold.schedule

import com.acme.transport.customs.clearance.variance.StrictClearanceGroup
import com.acme.transport.customs.clearance.variance.clearanceWindowBucket

fun summarizeStrictClearanceGroupsForSchedule(items: MutableList<StrictClearanceGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveHoldSchedulesFromGroup(items: MutableList<StrictClearanceGroup>): MutableList<HoldSchedule> {
    val out = mutableListOf<HoldSchedule>()
    for (item in items) {
        val derived = newHoldSchedule(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateHoldSchedule(item: HoldSchedule): String = clearanceWindowBucket(item.effectiveLoad())
