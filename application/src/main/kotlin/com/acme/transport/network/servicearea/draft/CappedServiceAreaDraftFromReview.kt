package com.acme.transport.network.servicearea.draft

import com.acme.transport.network.hub.audit.HubReview
import com.acme.transport.network.hub.audit.normalizeScheduledHubAuditCode

fun summarizeHubReviewsForDraft(items: MutableList<HubReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveCappedServiceAreaDraftsFromReview(items: MutableList<HubReview>): MutableList<CappedServiceAreaDraft> {
    val out = mutableListOf<CappedServiceAreaDraft>()
    for (item in items) {
        val derived = newCappedServiceAreaDraft(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeCappedServiceAreaDraft(item: CappedServiceAreaDraft): String {
    return normalizeScheduledHubAuditCode(item.code)
}
