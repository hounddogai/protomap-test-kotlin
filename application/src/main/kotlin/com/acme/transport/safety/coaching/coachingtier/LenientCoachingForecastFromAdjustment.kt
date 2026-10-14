package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.safety.nearmiss.variance.TieredNearMissAdjustment
import com.acme.transport.safety.nearmiss.variance.classifyBatchedNearMissSegment

fun summarizeTieredNearMissAdjustmentsForForecast(items: MutableList<TieredNearMissAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveLenientCoachingForecastsFromAdjustment(items: MutableList<TieredNearMissAdjustment>): MutableList<LenientCoachingForecast> {
    val out = mutableListOf<LenientCoachingForecast>()
    for (item in items) {
        val derived = newLenientCoachingForecast(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLenientCoachingForecast(item: LenientCoachingForecast): String {
    return classifyBatchedNearMissSegment(item.adjustedLoad())
}
