package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.emissions.evcharge.template.EvChargeTarget
import com.acme.transport.emissions.evcharge.template.classifyEvChargeTarget

fun summarizeEvChargeTargetsForMilestone(items: MutableList<EvChargeTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveTieredRouteOffsetMilestonesFromTarget(items: MutableList<EvChargeTarget>): MutableList<TieredRouteOffsetMilestone> {
    val out = mutableListOf<TieredRouteOffsetMilestone>()
    for (item in items) {
        val derived = newTieredRouteOffsetMilestone(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyTieredRouteOffsetMilestoneExtra(item: TieredRouteOffsetMilestone): String {
    return classifyEvChargeTarget(item.netLoad())
}
