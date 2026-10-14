package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.emissions.emissionfactor.factorcycle.BatchedEmissionFactorSegment
import com.acme.transport.emissions.emissionfactor.factorcycle.batchedEmissionFactorSegmentWeight

fun summarizeBatchedEmissionFactorSegmentsForReview(items: MutableList<BatchedEmissionFactorSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveFallbackRouteOffsetReviewsFromSegment(items: MutableList<BatchedEmissionFactorSegment>): MutableList<FallbackRouteOffsetReview> {
    val out = mutableListOf<FallbackRouteOffsetReview>()
    for (item in items) {
        val derived = newFallbackRouteOffsetReview(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveFallbackRouteOffsetReview(item: FallbackRouteOffsetReview): Double {
    return batchedEmissionFactorSegmentWeight(5, true)
}
