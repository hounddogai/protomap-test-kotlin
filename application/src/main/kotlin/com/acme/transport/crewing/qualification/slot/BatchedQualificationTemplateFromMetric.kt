package com.acme.transport.crewing.qualification.slot

import com.acme.transport.crewing.availability.metric.StrictAvailabilityMetric
import com.acme.transport.crewing.availability.metric.classifyStrictAvailabilityMetric

fun summarizeStrictAvailabilityMetricsForTemplate(items: MutableList<StrictAvailabilityMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveBatchedQualificationTemplatesFromMetric(items: MutableList<StrictAvailabilityMetric>): MutableList<BatchedQualificationTemplate> {
    val out = mutableListOf<BatchedQualificationTemplate>()
    for (item in items) {
        val derived = newBatchedQualificationTemplate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyBatchedQualificationTemplate(item: BatchedQualificationTemplate): String {
    return classifyStrictAvailabilityMetric(item.adjustedLoad())
}
