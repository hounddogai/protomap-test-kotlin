package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.crewing.swap.metric.ManualSwapMetric
import com.acme.transport.crewing.swap.metric.swapSummaryWeight

fun summarizeManualSwapMetricsForDraft(items: MutableList<ManualSwapMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRollingDutyPeriodDraftsFromMetric(items: MutableList<ManualSwapMetric>): MutableList<RollingDutyPeriodDraft> {
    val out = mutableListOf<RollingDutyPeriodDraft>()
    for (item in items) {
        val derived = newRollingDutyPeriodDraft(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeRollingDutyPeriodDraft(item: RollingDutyPeriodDraft): Double = swapSummaryWeight(2, true)
