package com.acme.transport.crewing.qualification.review

import com.acme.transport.crewing.restbreak.breaksummary.SeasonalRestBreakEstimate
import com.acme.transport.crewing.restbreak.breaksummary.normalizeSeasonalRestBreakEstimateCode

fun summarizeSeasonalRestBreakEstimatesForAllocation(items: MutableList<SeasonalRestBreakEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveSeasonalQualificationAllocationsFromEstimate(items: MutableList<SeasonalRestBreakEstimate>): MutableList<SeasonalQualificationAllocation> {
    val out = mutableListOf<SeasonalQualificationAllocation>()
    for (item in items) {
        val derived = newSeasonalQualificationAllocation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateSeasonalQualificationAllocation(item: SeasonalQualificationAllocation): String {
    return normalizeSeasonalRestBreakEstimateCode(item.code)
}
