package com.acme.transport.customs.declaration.agreement

import com.acme.transport.customs.inspection.inspectionoverride.FallbackInspectionMetric
import com.acme.transport.customs.inspection.inspectionoverride.normalizeInspectionCycleCode

fun summarizeFallbackInspectionMetricsForCheckpoint(items: MutableList<FallbackInspectionMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveBatchedDeclarationCheckpointsFromMetric(items: MutableList<FallbackInspectionMetric>): MutableList<BatchedDeclarationCheckpoint> {
    val out = mutableListOf<BatchedDeclarationCheckpoint>()
    for (item in items) {
        val derived = newBatchedDeclarationCheckpoint(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeBatchedDeclarationCheckpoint(item: BatchedDeclarationCheckpoint): String {
    return normalizeInspectionCycleCode(item.code)
}
