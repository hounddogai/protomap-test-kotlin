package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.lastmile.parcel.review.WeightedParcelThreshold
import com.acme.transport.lastmile.parcel.review.normalizeParcelDraftCode

fun summarizeWeightedParcelThresholdsForMetric(items: MutableList<WeightedParcelThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveParcelMetricsFromThreshold(items: MutableList<WeightedParcelThreshold>): MutableList<ParcelMetric> {
    val out = mutableListOf<ParcelMetric>()
    for (item in items) {
        val derived = newParcelMetric(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankParcelMetric(item: ParcelMetric): String = normalizeParcelDraftCode(item.code)
