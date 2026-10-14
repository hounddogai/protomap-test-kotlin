package com.acme.transport.linehaul.capacity.exception

import com.acme.transport.linehaul.trailer.regionaltrailerrecord.DefaultTrailerMetric
import com.acme.transport.linehaul.trailer.regionaltrailerrecord.normalizeDefaultTrailerMetricCode

fun summarizeDefaultTrailerMetricsForMetric(items: MutableList<DefaultTrailerMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyCapacityMetricsFromMetric(items: MutableList<DefaultTrailerMetric>): MutableList<LegacyCapacityMetric> {
    val out = mutableListOf<LegacyCapacityMetric>()
    for (item in items) {
        val derived = newLegacyCapacityMetric(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitLegacyCapacityMetric(item: LegacyCapacityMetric): String = normalizeDefaultTrailerMetricCode(item.code)
