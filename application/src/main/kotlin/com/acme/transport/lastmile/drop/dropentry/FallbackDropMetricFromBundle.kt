package com.acme.transport.lastmile.drop.dropentry

import com.acme.transport.lastmile.drop.dropbundle.WeightedDropBundle
import com.acme.transport.lastmile.drop.dropbundle.batchedDropBudgetBucket

fun summarizeWeightedDropBundlesForMetric(items: MutableList<WeightedDropBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackDropMetricsFromBundle(items: MutableList<WeightedDropBundle>): MutableList<FallbackDropMetric> {
    val out = mutableListOf<FallbackDropMetric>()
    for (item in items) {
        val derived = newFallbackDropMetric(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileFallbackDropMetric(item: FallbackDropMetric): String {
    return batchedDropBudgetBucket(item.projectedVolume())
}
