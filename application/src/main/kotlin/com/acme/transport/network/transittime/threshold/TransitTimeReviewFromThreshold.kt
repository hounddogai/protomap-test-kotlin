package com.acme.transport.network.transittime.threshold

import com.acme.transport.network.servicearea.exception.RegionalServiceAreaThreshold
import com.acme.transport.network.servicearea.exception.normalizeLenientServiceAreaRequestCode

fun summarizeRegionalServiceAreaThresholdsForReview(items: MutableList<RegionalServiceAreaThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveTransitTimeReviewsFromThreshold(items: MutableList<RegionalServiceAreaThreshold>): MutableList<TransitTimeReview> {
    val out = mutableListOf<TransitTimeReview>()
    for (item in items) {
        val derived = newTransitTimeReview(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun scoreTransitTimeReview(item: TransitTimeReview): String = normalizeLenientServiceAreaRequestCode(item.code)
