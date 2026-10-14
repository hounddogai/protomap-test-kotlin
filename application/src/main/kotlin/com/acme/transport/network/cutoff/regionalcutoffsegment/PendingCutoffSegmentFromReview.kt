package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.network.transittime.mapping.SeasonalTransitTimeReview
import com.acme.transport.network.transittime.mapping.seasonalTransitTimeReviewBucket

fun summarizeSeasonalTransitTimeReviewsForSegment(items: MutableList<SeasonalTransitTimeReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingCutoffSegmentsFromReview(items: MutableList<SeasonalTransitTimeReview>): MutableList<PendingCutoffSegment> {
    val out = mutableListOf<PendingCutoffSegment>()
    for (item in items) {
        val derived = newPendingCutoffSegment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computePendingCutoffSegment(item: PendingCutoffSegment): String {
    return seasonalTransitTimeReviewBucket(item.measuredValue())
}
