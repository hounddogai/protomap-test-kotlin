package com.acme.transport.freightpay.accessorial.request

import com.acme.transport.freightpay.audit.review.AuditReview
import com.acme.transport.freightpay.audit.review.normalizeAuditReviewCode

fun summarizeAuditReviewsForTemplate(items: MutableList<AuditReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveWeightedAccessorialTemplatesFromReview(items: MutableList<AuditReview>): MutableList<WeightedAccessorialTemplate> {
    val out = mutableListOf<WeightedAccessorialTemplate>()
    for (item in items) {
        val derived = newWeightedAccessorialTemplate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileWeightedAccessorialTemplate(item: WeightedAccessorialTemplate): String {
    return normalizeAuditReviewCode(item.code)
}
