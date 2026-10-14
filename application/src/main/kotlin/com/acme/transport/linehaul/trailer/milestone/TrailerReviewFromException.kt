package com.acme.transport.linehaul.trailer.milestone

import com.acme.transport.linehaul.laneplan.tier.AdaptiveLanePlanException
import com.acme.transport.linehaul.laneplan.tier.normalizeLanePlanTierCode

fun summarizeAdaptiveLanePlanExceptionsForReview(items: MutableList<AdaptiveLanePlanException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveTrailerReviewsFromException(items: MutableList<AdaptiveLanePlanException>): MutableList<TrailerReview> {
    val out = mutableListOf<TrailerReview>()
    for (item in items) {
        val derived = newTrailerReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateTrailerReview(item: TrailerReview): String = normalizeLanePlanTierCode(item.code)
