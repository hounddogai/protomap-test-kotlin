package com.acme.transport.safety.violation.batch

import com.acme.transport.safety.coaching.coachingdigest.ScheduledCoachingSummary
import com.acme.transport.safety.coaching.coachingdigest.classifyTieredCoachingAudit

fun summarizeScheduledCoachingSummariesForSlot(items: MutableList<ScheduledCoachingSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveBatchedViolationSlotsFromSummary(items: MutableList<ScheduledCoachingSummary>): MutableList<BatchedViolationSlot> {
    val out = mutableListOf<BatchedViolationSlot>()
    for (item in items) {
        val derived = newBatchedViolationSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyBatchedViolationSlot(item: BatchedViolationSlot): String {
    return classifyTieredCoachingAudit(item.measuredValue())
}
