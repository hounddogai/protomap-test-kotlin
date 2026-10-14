package com.acme.transport.crewing.swap.metric

import com.acme.transport.crewing.restbreak.overrideitem.LegacyRestBreakEstimate
import com.acme.transport.crewing.restbreak.overrideitem.normalizeLegacyRestBreakMappingCode

fun summarizeLegacyRestBreakEstimatesForMetric(items: MutableList<LegacyRestBreakEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveManualSwapMetricsFromEstimate(items: MutableList<LegacyRestBreakEstimate>): MutableList<ManualSwapMetric> {
    val out = mutableListOf<ManualSwapMetric>()
    for (item in items) {
        val derived = newManualSwapMetric(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateManualSwapMetric(item: ManualSwapMetric): String = normalizeLegacyRestBreakMappingCode(item.code)
