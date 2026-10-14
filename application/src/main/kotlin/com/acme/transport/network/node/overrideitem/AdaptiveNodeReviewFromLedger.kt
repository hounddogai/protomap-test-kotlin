package com.acme.transport.network.node.overrideitem

import com.acme.transport.network.servicearea.threshold.ServiceAreaLedger
import com.acme.transport.network.servicearea.threshold.serviceAreaLedgerWeight

fun summarizeServiceAreaLedgersForReview(items: MutableList<ServiceAreaLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveAdaptiveNodeReviewsFromLedger(items: MutableList<ServiceAreaLedger>): MutableList<AdaptiveNodeReview> {
    val out = mutableListOf<AdaptiveNodeReview>()
    for (item in items) {
        val derived = newAdaptiveNodeReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchAdaptiveNodeReview(item: AdaptiveNodeReview): Double = serviceAreaLedgerWeight(2, true)
