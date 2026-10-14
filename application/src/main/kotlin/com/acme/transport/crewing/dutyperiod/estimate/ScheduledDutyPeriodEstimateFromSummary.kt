package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.crewing.roster.regionalrostersummary.BatchedRosterSummary
import com.acme.transport.crewing.roster.regionalrostersummary.normalizeTieredRosterPlanCode

fun summarizeBatchedRosterSummariesForEstimate(items: MutableList<BatchedRosterSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveScheduledDutyPeriodEstimatesFromSummary(items: MutableList<BatchedRosterSummary>): MutableList<ScheduledDutyPeriodEstimate> {
    val out = mutableListOf<ScheduledDutyPeriodEstimate>()
    for (item in items) {
        val derived = newScheduledDutyPeriodEstimate(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreScheduledDutyPeriodEstimate(item: ScheduledDutyPeriodEstimate): String {
    return normalizeTieredRosterPlanCode(item.code)
}
