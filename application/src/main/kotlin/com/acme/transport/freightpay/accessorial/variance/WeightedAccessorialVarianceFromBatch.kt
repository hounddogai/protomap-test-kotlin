package com.acme.transport.freightpay.accessorial.variance

import com.acme.transport.freightpay.accessorial.baseline.TieredAccessorialBatch
import com.acme.transport.freightpay.accessorial.baseline.classifyTieredAccessorialBatch

fun summarizeTieredAccessorialBatchesForVariance(items: MutableList<TieredAccessorialBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveWeightedAccessorialVariancesFromBatch(items: MutableList<TieredAccessorialBatch>): MutableList<WeightedAccessorialVariance> {
    val out = mutableListOf<WeightedAccessorialVariance>()
    for (item in items) {
        val derived = newWeightedAccessorialVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreWeightedAccessorialVariance(item: WeightedAccessorialVariance): String {
    return classifyTieredAccessorialBatch(item.effectiveValue())
}
