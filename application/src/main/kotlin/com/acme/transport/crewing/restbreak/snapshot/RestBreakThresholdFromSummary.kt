package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.crewing.restbreak.draft.RestBreakSummary
import com.acme.transport.crewing.restbreak.draft.restBreakSummaryWeight

fun summarizeRestBreakSummariesForThreshold(items: MutableList<RestBreakSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveRestBreakThresholdsFromSummary(items: MutableList<RestBreakSummary>): MutableList<RestBreakThreshold> {
    val out = mutableListOf<RestBreakThreshold>()
    for (item in items) {
        val derived = newRestBreakThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeRestBreakThreshold(item: RestBreakThreshold): Double = restBreakSummaryWeight(8, true)
