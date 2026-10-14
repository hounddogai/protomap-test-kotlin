package com.acme.transport.linehaul.trailer.bundle

import com.acme.transport.linehaul.trailer.rollup.ManualTrailerSegment
import com.acme.transport.linehaul.trailer.rollup.normalizeManualTrailerSegmentCode

fun summarizeManualTrailerSegmentsForVariance(items: MutableList<ManualTrailerSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveLegacyTrailerVariancesFromSegment(items: MutableList<ManualTrailerSegment>): MutableList<LegacyTrailerVariance> {
    val out = mutableListOf<LegacyTrailerVariance>()
    for (item in items) {
        val derived = newLegacyTrailerVariance(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileLegacyTrailerVariance(item: LegacyTrailerVariance): String {
    return normalizeManualTrailerSegmentCode(item.code)
}
