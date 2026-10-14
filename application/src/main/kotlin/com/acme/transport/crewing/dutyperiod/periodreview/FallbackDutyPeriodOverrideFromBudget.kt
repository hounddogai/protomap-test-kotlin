package com.acme.transport.crewing.dutyperiod.periodreview

import com.acme.transport.crewing.availability.availabilitydigest.ManualAvailabilityBudget
import com.acme.transport.crewing.availability.availabilitydigest.manualAvailabilityBudgetWeight

fun summarizeManualAvailabilityBudgetsForOverride(items: MutableList<ManualAvailabilityBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveFallbackDutyPeriodOverridesFromBudget(items: MutableList<ManualAvailabilityBudget>): MutableList<FallbackDutyPeriodOverride> {
    val out = mutableListOf<FallbackDutyPeriodOverride>()
    for (item in items) {
        val derived = newFallbackDutyPeriodOverride(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planFallbackDutyPeriodOverride(item: FallbackDutyPeriodOverride): Double {
    return manualAvailabilityBudgetWeight(1, true)
}
