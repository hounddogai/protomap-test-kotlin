package com.acme.transport.crewing.swap.entry

import com.acme.transport.crewing.dutyperiod.periodreview.FallbackDutyPeriodOverride
import com.acme.transport.crewing.dutyperiod.periodreview.rollingDutyPeriodForecastWeight

fun summarizeFallbackDutyPeriodOverridesForEntry(items: MutableList<FallbackDutyPeriodOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRegionalSwapEntriesFromOverride(items: MutableList<FallbackDutyPeriodOverride>): MutableList<RegionalSwapEntry> {
    val out = mutableListOf<RegionalSwapEntry>()
    for (item in items) {
        val derived = newRegionalSwapEntry(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateRegionalSwapEntry(item: RegionalSwapEntry): Double = rollingDutyPeriodForecastWeight(6, true)
