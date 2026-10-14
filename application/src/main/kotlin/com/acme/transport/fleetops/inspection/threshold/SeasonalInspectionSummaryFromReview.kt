package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.fleetops.utilization.milestone.StrictUtilizationReview
import com.acme.transport.fleetops.utilization.milestone.normalizeLegacyUtilizationSlotCode

fun summarizeStrictUtilizationReviewsForSummary(items: MutableList<StrictUtilizationReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveSeasonalInspectionSummariesFromReview(items: MutableList<StrictUtilizationReview>): MutableList<SeasonalInspectionSummary> {
    val out = mutableListOf<SeasonalInspectionSummary>()
    for (item in items) {
        val derived = newSeasonalInspectionSummary(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveSeasonalInspectionSummary(item: SeasonalInspectionSummary): String {
    return normalizeLegacyUtilizationSlotCode(item.code)
}
