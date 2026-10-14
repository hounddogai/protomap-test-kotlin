package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.emissions.routeoffset.metric.RouteOffsetClaim
import com.acme.transport.emissions.routeoffset.metric.normalizeWeightedRouteOffsetTargetCode

fun summarizeRouteOffsetClaimsForReview(items: MutableList<RouteOffsetClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveEvChargeReviewsFromClaim(items: MutableList<RouteOffsetClaim>): MutableList<EvChargeReview> {
    val out = mutableListOf<EvChargeReview>()
    for (item in items) {
        val derived = newEvChargeReview(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateEvChargeReview(item: EvChargeReview): String = normalizeWeightedRouteOffsetTargetCode(item.code)
