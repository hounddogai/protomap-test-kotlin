package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.fleetops.serviceplan.threshold.SeasonalServicePlanBudget
import com.acme.transport.fleetops.serviceplan.threshold.seasonalServicePlanBudgetWeight

fun summarizeSeasonalServicePlanBudgetsForRollup(items: MutableList<SeasonalServicePlanBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveTireRollupsFromBudget(items: MutableList<SeasonalServicePlanBudget>): MutableList<TireRollup> {
    val out = mutableListOf<TireRollup>()
    for (item in items) {
        val derived = newTireRollup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeTireRollup(item: TireRollup): Double = seasonalServicePlanBudgetWeight(4, true)
