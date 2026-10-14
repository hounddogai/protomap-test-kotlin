package com.acme.transport.customs.declaration.batch

import com.acme.transport.customs.clearance.metric.ClearanceMetric
import com.acme.transport.customs.clearance.metric.clearanceMetricBucket

fun summarizeClearanceMetricsForBatch(items: MutableList<ClearanceMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveDeclarationBatchesFromMetric(items: MutableList<ClearanceMetric>): MutableList<DeclarationBatch> {
    val out = mutableListOf<DeclarationBatch>()
    for (item in items) {
        val derived = newDeclarationBatch(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeDeclarationBatch(item: DeclarationBatch): String = clearanceMetricBucket(item.projectedExposure())
