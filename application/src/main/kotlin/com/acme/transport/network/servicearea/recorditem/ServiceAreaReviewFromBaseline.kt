package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.network.hub.estimate.LenientHubBaseline
import com.acme.transport.network.hub.estimate.lenientHubBaselineWeight

fun summarizeLenientHubBaselinesForReview(items: MutableList<LenientHubBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveServiceAreaReviewsFromBaseline(items: MutableList<LenientHubBaseline>): MutableList<ServiceAreaReview> {
    val out = mutableListOf<ServiceAreaReview>()
    for (item in items) {
        val derived = newServiceAreaReview(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignServiceAreaReview(item: ServiceAreaReview): Double = lenientHubBaselineWeight(2, true)
