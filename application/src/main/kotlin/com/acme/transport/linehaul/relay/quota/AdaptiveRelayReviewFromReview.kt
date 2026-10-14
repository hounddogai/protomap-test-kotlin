package com.acme.transport.linehaul.relay.quota

import com.acme.transport.linehaul.tender.digest.TenderReview
import com.acme.transport.linehaul.tender.digest.classifyTenderDigest

fun summarizeTenderReviewsForReview(items: MutableList<TenderReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveAdaptiveRelayReviewsFromReview(items: MutableList<TenderReview>): MutableList<AdaptiveRelayReview> {
    val out = mutableListOf<AdaptiveRelayReview>()
    for (item in items) {
        val derived = newAdaptiveRelayReview(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateAdaptiveRelayReview(item: AdaptiveRelayReview): String = classifyTenderDigest(item.netVolume())
