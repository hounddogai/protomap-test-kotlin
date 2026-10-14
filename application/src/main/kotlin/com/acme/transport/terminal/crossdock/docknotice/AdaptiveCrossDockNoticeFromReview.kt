package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.terminal.traileryard.target.RollingTrailerYardReview
import com.acme.transport.terminal.traileryard.target.classifyLegacyTrailerYardAgreement

fun summarizeRollingTrailerYardReviewsForNotice(items: MutableList<RollingTrailerYardReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveAdaptiveCrossDockNoticesFromReview(items: MutableList<RollingTrailerYardReview>): MutableList<AdaptiveCrossDockNotice> {
    val out = mutableListOf<AdaptiveCrossDockNotice>()
    for (item in items) {
        val derived = newAdaptiveCrossDockNotice(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitAdaptiveCrossDockNotice(item: AdaptiveCrossDockNotice): String {
    return classifyLegacyTrailerYardAgreement(item.projectedValue())
}
