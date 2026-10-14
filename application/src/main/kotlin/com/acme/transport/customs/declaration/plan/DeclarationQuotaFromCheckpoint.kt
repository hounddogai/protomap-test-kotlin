package com.acme.transport.customs.declaration.plan

import com.acme.transport.customs.declaration.metric.WeightedDeclarationCheckpoint
import com.acme.transport.customs.declaration.metric.declarationMetricBucket

fun summarizeWeightedDeclarationCheckpointsForQuota(items: MutableList<WeightedDeclarationCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveDeclarationQuotasFromCheckpoint(items: MutableList<WeightedDeclarationCheckpoint>): MutableList<DeclarationQuota> {
    val out = mutableListOf<DeclarationQuota>()
    for (item in items) {
        val derived = newDeclarationQuota(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignDeclarationQuota(item: DeclarationQuota): String = declarationMetricBucket(item.weightedLoad())
