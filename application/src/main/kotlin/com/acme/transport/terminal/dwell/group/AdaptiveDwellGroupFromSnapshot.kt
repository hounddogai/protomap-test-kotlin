package com.acme.transport.terminal.dwell.group

import com.acme.transport.terminal.dwell.snapshot.ScheduledDwellSnapshot
import com.acme.transport.terminal.dwell.snapshot.lenientDwellPolicyBucket

fun summarizeScheduledDwellSnapshotsForGroup(items: MutableList<ScheduledDwellSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveAdaptiveDwellGroupsFromSnapshot(items: MutableList<ScheduledDwellSnapshot>): MutableList<AdaptiveDwellGroup> {
    val out = mutableListOf<AdaptiveDwellGroup>()
    for (item in items) {
        val derived = newAdaptiveDwellGroup(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyAdaptiveDwellGroup(item: AdaptiveDwellGroup): String = lenientDwellPolicyBucket(item.measuredValue())
