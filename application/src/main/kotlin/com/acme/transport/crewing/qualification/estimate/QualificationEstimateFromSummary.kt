package com.acme.transport.crewing.qualification.estimate

import com.acme.transport.crewing.restbreak.draft.RestBreakSummary
import com.acme.transport.crewing.restbreak.draft.restBreakSummaryWeight

fun summarizeRestBreakSummariesForEstimate(items: MutableList<RestBreakSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveQualificationEstimatesFromSummary(items: MutableList<RestBreakSummary>): MutableList<QualificationEstimate> {
    val out = mutableListOf<QualificationEstimate>()
    for (item in items) {
        val derived = newQualificationEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateQualificationEstimate(item: QualificationEstimate): Double = restBreakSummaryWeight(4, true)
