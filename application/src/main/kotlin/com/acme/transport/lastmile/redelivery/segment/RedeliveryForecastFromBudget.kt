package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.lastmile.locker.snapshot.AdaptiveLockerBudget
import com.acme.transport.lastmile.locker.snapshot.lockerPlanWeight

fun summarizeAdaptiveLockerBudgetsForForecast(items: MutableList<AdaptiveLockerBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRedeliveryForecastsFromBudget(items: MutableList<AdaptiveLockerBudget>): MutableList<RedeliveryForecast> {
    val out = mutableListOf<RedeliveryForecast>()
    for (item in items) {
        val derived = newRedeliveryForecast(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreRedeliveryForecast(item: RedeliveryForecast): Double = lockerPlanWeight(2, true)
