package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.crewing.swap.swapmapping.FallbackSwapSegment
import com.acme.transport.crewing.swap.swapmapping.weightedSwapMappingWeight

fun summarizeFallbackSwapSegmentsForMetric(items: MutableList<FallbackSwapSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRestBreakMetricsFromSegment(items: MutableList<FallbackSwapSegment>): MutableList<RestBreakMetric> {
    val out = mutableListOf<RestBreakMetric>()
    for (item in items) {
        val derived = newRestBreakMetric(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planRestBreakMetric(item: RestBreakMetric): Double = weightedSwapMappingWeight(8, true)
