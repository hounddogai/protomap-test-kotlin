package com.acme.transport.safety.coaching.bundle

import com.acme.transport.safety.nearmiss.slot.NearMissGroup
import com.acme.transport.safety.nearmiss.slot.nearMissSlotBucket

fun summarizeNearMissGroupsForGroup(items: MutableList<NearMissGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveCoachingGroupsFromGroup(items: MutableList<NearMissGroup>): MutableList<CoachingGroup> {
    val out = mutableListOf<CoachingGroup>()
    for (item in items) {
        val derived = newCoachingGroup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyCoachingGroup(item: CoachingGroup): String = nearMissSlotBucket(item.measuredValue())
