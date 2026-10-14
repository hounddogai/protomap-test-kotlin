package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.crewing.availability.forecast.AvailabilityBudget
import com.acme.transport.crewing.availability.forecast.classifyAvailabilityForecast

fun summarizeAvailabilityBudgetsForThreshold(items: MutableList<AvailabilityBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveDutyPeriodThresholdsFromBudget(items: MutableList<AvailabilityBudget>): MutableList<DutyPeriodThreshold> {
    val out = mutableListOf<DutyPeriodThreshold>()
    for (item in items) {
        val derived = newDutyPeriodThreshold(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeDutyPeriodThreshold(item: DutyPeriodThreshold): String {
    return classifyAvailabilityForecast(item.effectiveLoad())
}
