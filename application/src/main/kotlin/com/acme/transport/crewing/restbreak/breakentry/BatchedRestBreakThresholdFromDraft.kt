package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.crewing.dutyperiod.window.RollingDutyPeriodDraft
import com.acme.transport.crewing.dutyperiod.window.classifySeasonalDutyPeriodBatch

fun summarizeRollingDutyPeriodDraftsForThreshold(items: MutableList<RollingDutyPeriodDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveBatchedRestBreakThresholdsFromDraft(items: MutableList<RollingDutyPeriodDraft>): MutableList<BatchedRestBreakThreshold> {
    val out = mutableListOf<BatchedRestBreakThreshold>()
    for (item in items) {
        val derived = newBatchedRestBreakThreshold(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceBatchedRestBreakThreshold(item: BatchedRestBreakThreshold): String {
    return classifySeasonalDutyPeriodBatch(item.effectiveTotal())
}
