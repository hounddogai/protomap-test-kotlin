package com.acme.transport.crewing.qualification.summary

import com.acme.transport.crewing.restbreak.metric.RestBreakMetric
import com.acme.transport.crewing.restbreak.metric.classifyWeightedRestBreakAllocation

fun summarizeRestBreakMetricsForAssignment(items: MutableList<RestBreakMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveBatchedQualificationAssignmentsFromMetric(items: MutableList<RestBreakMetric>): MutableList<BatchedQualificationAssignment> {
    val out = mutableListOf<BatchedQualificationAssignment>()
    for (item in items) {
        val derived = newBatchedQualificationAssignment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreBatchedQualificationAssignment(item: BatchedQualificationAssignment): String {
    return classifyWeightedRestBreakAllocation(item.projectedLoad())
}
