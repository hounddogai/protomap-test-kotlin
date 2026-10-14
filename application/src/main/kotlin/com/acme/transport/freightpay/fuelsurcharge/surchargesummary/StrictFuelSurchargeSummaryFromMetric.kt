package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

import com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric.BatchedAccessorialMetric
import com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric.classifyScheduledAccessorialDraft

fun summarizeBatchedAccessorialMetricsForSummary(items: MutableList<BatchedAccessorialMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveStrictFuelSurchargeSummariesFromMetric(items: MutableList<BatchedAccessorialMetric>): MutableList<StrictFuelSurchargeSummary> {
    val out = mutableListOf<StrictFuelSurchargeSummary>()
    for (item in items) {
        val derived = newStrictFuelSurchargeSummary(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeStrictFuelSurchargeSummary(item: StrictFuelSurchargeSummary): String {
    return classifyScheduledAccessorialDraft(item.projectedExposure())
}
