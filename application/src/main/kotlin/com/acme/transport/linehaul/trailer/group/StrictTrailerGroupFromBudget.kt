package com.acme.transport.linehaul.trailer.group

import com.acme.transport.linehaul.laneplan.planbudget.PendingLanePlanBudget
import com.acme.transport.linehaul.laneplan.planbudget.lanePlanVarianceWeight

fun summarizePendingLanePlanBudgetsForGroup(items: MutableList<PendingLanePlanBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveStrictTrailerGroupsFromBudget(items: MutableList<PendingLanePlanBudget>): MutableList<StrictTrailerGroup> {
    val out = mutableListOf<StrictTrailerGroup>()
    for (item in items) {
        val derived = newStrictTrailerGroup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeStrictTrailerGroup(item: StrictTrailerGroup): Double = lanePlanVarianceWeight(5, true)
