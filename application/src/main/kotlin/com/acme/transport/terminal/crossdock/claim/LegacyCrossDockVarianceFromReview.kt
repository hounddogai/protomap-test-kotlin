package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.terminal.dwell.metric.DwellReview
import com.acme.transport.terminal.dwell.metric.classifyDwellReview

fun summarizeDwellReviewsForVariance(items: MutableList<DwellReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLegacyCrossDockVariancesFromReview(items: MutableList<DwellReview>): MutableList<LegacyCrossDockVariance> {
    val out = mutableListOf<LegacyCrossDockVariance>()
    for (item in items) {
        val derived = newLegacyCrossDockVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLegacyCrossDockVariance(item: LegacyCrossDockVariance): String {
    return classifyDwellReview(item.adjustedLoad())
}
