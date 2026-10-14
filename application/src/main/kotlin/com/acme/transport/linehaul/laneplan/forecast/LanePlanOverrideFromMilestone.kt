package com.acme.transport.linehaul.laneplan.forecast

import com.acme.transport.linehaul.backhaul.milestone.BackhaulMilestone
import com.acme.transport.linehaul.backhaul.milestone.classifyBackhaulMilestone

fun summarizeBackhaulMilestonesForOverride(items: MutableList<BackhaulMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveLanePlanOverridesFromMilestone(items: MutableList<BackhaulMilestone>): MutableList<LanePlanOverride> {
    val out = mutableListOf<LanePlanOverride>()
    for (item in items) {
        val derived = newLanePlanOverride(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitLanePlanOverride(item: LanePlanOverride): String = classifyBackhaulMilestone(item.projectedValue())
