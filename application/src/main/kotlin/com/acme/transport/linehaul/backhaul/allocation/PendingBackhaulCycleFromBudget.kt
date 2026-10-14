package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.linehaul.laneplan.planbudget.PendingLanePlanBudget
import com.acme.transport.linehaul.laneplan.planbudget.seasonalLanePlanEstimateWeight

fun summarizePendingLanePlanBudgetsForCycle(items: MutableList<PendingLanePlanBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun derivePendingBackhaulCyclesFromBudget(items: MutableList<PendingLanePlanBudget>): MutableList<PendingBackhaulCycle> {
    val out = mutableListOf<PendingBackhaulCycle>()
    for (item in items) {
        val derived = newPendingBackhaulCycle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectPendingBackhaulCycle(item: PendingBackhaulCycle): Double = seasonalLanePlanEstimateWeight(8, true)
