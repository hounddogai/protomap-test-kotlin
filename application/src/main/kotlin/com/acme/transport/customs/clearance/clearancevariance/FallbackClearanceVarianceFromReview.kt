package com.acme.transport.customs.clearance.clearancevariance

import com.acme.transport.customs.broker.review.SeasonalBrokerReview
import com.acme.transport.customs.broker.review.regionalBrokerRequestWeight

fun summarizeSeasonalBrokerReviewsForVariance(items: MutableList<SeasonalBrokerReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveFallbackClearanceVariancesFromReview(items: MutableList<SeasonalBrokerReview>): MutableList<FallbackClearanceVariance> {
    val out = mutableListOf<FallbackClearanceVariance>()
    for (item in items) {
        val derived = newFallbackClearanceVariance(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitFallbackClearanceVariance(item: FallbackClearanceVariance): Double = regionalBrokerRequestWeight(9, true)
