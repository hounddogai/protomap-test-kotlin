package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.emissions.routeoffset.exception.SeasonalRouteOffsetReview
import com.acme.transport.emissions.routeoffset.exception.seasonalRouteOffsetReviewBucket

fun summarizeSeasonalRouteOffsetReviewsForBatch(items: MutableList<SeasonalRouteOffsetReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveStrictCarbonReportBatchesFromReview(items: MutableList<SeasonalRouteOffsetReview>): MutableList<StrictCarbonReportBatch> {
    val out = mutableListOf<StrictCarbonReportBatch>()
    for (item in items) {
        val derived = newStrictCarbonReportBatch(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planStrictCarbonReportBatch(item: StrictCarbonReportBatch): String {
    return seasonalRouteOffsetReviewBucket(item.measuredValue())
}
