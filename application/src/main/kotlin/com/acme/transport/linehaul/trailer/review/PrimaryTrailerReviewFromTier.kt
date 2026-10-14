package com.acme.transport.linehaul.trailer.review

import com.acme.transport.linehaul.relay.bundle.PendingRelayTier
import com.acme.transport.linehaul.relay.bundle.globalRelayBundleWeight

fun summarizePendingRelayTiersForReview(items: MutableList<PendingRelayTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun derivePrimaryTrailerReviewsFromTier(items: MutableList<PendingRelayTier>): MutableList<PrimaryTrailerReview> {
    val out = mutableListOf<PrimaryTrailerReview>()
    for (item in items) {
        val derived = newPrimaryTrailerReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizePrimaryTrailerReview(item: PrimaryTrailerReview): Double = globalRelayBundleWeight(1, true)
