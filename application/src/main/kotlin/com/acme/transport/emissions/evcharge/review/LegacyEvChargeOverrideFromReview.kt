package com.acme.transport.emissions.evcharge.review

import com.acme.transport.emissions.carbonreport.mapping.CarbonReportReview
import com.acme.transport.emissions.carbonreport.mapping.carbonReportReviewBucket

fun summarizeCarbonReportReviewsForOverride(items: MutableList<CarbonReportReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveLegacyEvChargeOverridesFromReview(items: MutableList<CarbonReportReview>): MutableList<LegacyEvChargeOverride> {
    val out = mutableListOf<LegacyEvChargeOverride>()
    for (item in items) {
        val derived = newLegacyEvChargeOverride(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitLegacyEvChargeOverride(item: LegacyEvChargeOverride): String {
    return carbonReportReviewBucket(item.effectiveTotal())
}
