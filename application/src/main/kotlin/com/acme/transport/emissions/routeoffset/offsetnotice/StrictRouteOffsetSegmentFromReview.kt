package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.emissions.routeoffset.exception.FallbackRouteOffsetReview
import com.acme.transport.emissions.routeoffset.exception.seasonalRouteOffsetReviewBucket

fun summarizeFallbackRouteOffsetReviewsForSegment(items: MutableList<FallbackRouteOffsetReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveStrictRouteOffsetSegmentsFromReview(items: MutableList<FallbackRouteOffsetReview>): MutableList<StrictRouteOffsetSegment> {
    val out = mutableListOf<StrictRouteOffsetSegment>()
    for (item in items) {
        val derived = newStrictRouteOffsetSegment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateStrictRouteOffsetSegment(item: StrictRouteOffsetSegment): String {
    return seasonalRouteOffsetReviewBucket(item.adjustedExposure())
}
