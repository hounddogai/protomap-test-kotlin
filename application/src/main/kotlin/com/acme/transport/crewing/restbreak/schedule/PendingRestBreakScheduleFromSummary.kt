package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.crewing.qualification.summary.BatchedQualificationSummary
import com.acme.transport.crewing.qualification.summary.batchedQualificationSummaryWeight

fun summarizeBatchedQualificationSummariesForSchedule(items: MutableList<BatchedQualificationSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun derivePendingRestBreakSchedulesFromSummary(items: MutableList<BatchedQualificationSummary>): MutableList<PendingRestBreakSchedule> {
    val out = mutableListOf<PendingRestBreakSchedule>()
    for (item in items) {
        val derived = newPendingRestBreakSchedule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocatePendingRestBreakSchedule(item: PendingRestBreakSchedule): Double {
    return batchedQualificationSummaryWeight(6, true)
}
