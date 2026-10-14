package com.acme.transport.network.transittime.timebudget

import com.acme.transport.network.hub.audit.HubReview
import com.acme.transport.network.hub.audit.normalizeScheduledHubAuditCode

fun summarizeHubReviewsForTarget(items: MutableList<HubReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveTransitTimeTargetsFromReview(items: MutableList<HubReview>): MutableList<TransitTimeTarget> {
    val out = mutableListOf<TransitTimeTarget>()
    for (item in items) {
        val derived = newTransitTimeTarget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceTransitTimeTarget(item: TransitTimeTarget): String = normalizeScheduledHubAuditCode(item.code)
