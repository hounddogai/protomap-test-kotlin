package com.acme.transport.linehaul.capacity.reservation

import com.acme.transport.linehaul.backhaul.milestone.BackhaulMilestone
import com.acme.transport.linehaul.backhaul.milestone.cappedBackhaulProfileBucket

fun summarizeBackhaulMilestonesForGroup(items: MutableList<BackhaulMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveTieredCapacityGroupsFromMilestone(items: MutableList<BackhaulMilestone>): MutableList<TieredCapacityGroup> {
    val out = mutableListOf<TieredCapacityGroup>()
    for (item in items) {
        val derived = newTieredCapacityGroup(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeTieredCapacityGroup(item: TieredCapacityGroup): String {
    return cappedBackhaulProfileBucket(item.projectedValue())
}
