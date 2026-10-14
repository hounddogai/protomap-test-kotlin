package com.acme.transport.network.node.manifest

import com.acme.transport.network.transittime.forecast.ManualTransitTimeBatch
import com.acme.transport.network.transittime.forecast.fallbackTransitTimeTemplateBucket

fun summarizeManualTransitTimeBatchesForEstimate(items: MutableList<ManualTransitTimeBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveTieredNodeEstimatesFromBatch(items: MutableList<ManualTransitTimeBatch>): MutableList<TieredNodeEstimate> {
    val out = mutableListOf<TieredNodeEstimate>()
    for (item in items) {
        val derived = newTieredNodeEstimate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeTieredNodeEstimate(item: TieredNodeEstimate): String {
    return fallbackTransitTimeTemplateBucket(item.weightedExposure())
}
