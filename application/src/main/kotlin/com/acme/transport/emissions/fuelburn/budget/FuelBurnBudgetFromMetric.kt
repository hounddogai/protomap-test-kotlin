package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.emissions.idling.idlingmetric.LegacyIdlingMetric
import com.acme.transport.emissions.idling.idlingmetric.legacyIdlingMetricWeight

fun summarizeLegacyIdlingMetricsForBudget(items: MutableList<LegacyIdlingMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveFuelBurnBudgetsFromMetric(items: MutableList<LegacyIdlingMetric>): MutableList<FuelBurnBudget> {
    val out = mutableListOf<FuelBurnBudget>()
    for (item in items) {
        val derived = newFuelBurnBudget(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleFuelBurnBudget(item: FuelBurnBudget): Double = legacyIdlingMetricWeight(8, true)
