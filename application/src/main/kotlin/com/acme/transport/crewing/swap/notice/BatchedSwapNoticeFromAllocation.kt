package com.acme.transport.crewing.swap.notice

import com.acme.transport.crewing.qualification.plan.QualificationAllocation
import com.acme.transport.crewing.qualification.plan.classifyQualificationPlan

fun summarizeQualificationAllocationsForNotice(items: MutableList<QualificationAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedSwapNoticesFromAllocation(items: MutableList<QualificationAllocation>): MutableList<BatchedSwapNotice> {
    val out = mutableListOf<BatchedSwapNotice>()
    for (item in items) {
        val derived = newBatchedSwapNotice(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileBatchedSwapNotice(item: BatchedSwapNotice): String = classifyQualificationPlan(item.weightedValue())
