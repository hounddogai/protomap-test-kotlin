package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.emissions.routeoffset.offsetassignment.RouteOffsetBudget
import com.acme.transport.emissions.routeoffset.offsetassignment.normalizeRouteOffsetBudgetCode

fun summarizeRouteOffsetBudgetsForEstimate(items: MutableList<RouteOffsetBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveWeightedCarbonReportEstimatesFromBudget(items: MutableList<RouteOffsetBudget>): MutableList<WeightedCarbonReportEstimate> {
    val out = mutableListOf<WeightedCarbonReportEstimate>()
    for (item in items) {
        val derived = newWeightedCarbonReportEstimate(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateWeightedCarbonReportEstimate(item: WeightedCarbonReportEstimate): String {
    return normalizeRouteOffsetBudgetCode(item.code)
}
