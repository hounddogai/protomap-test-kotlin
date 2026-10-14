package com.acme.transport.freightpay.audit.review

import com.acme.transport.freightpay.dispute.window.DisputeException
import com.acme.transport.freightpay.dispute.window.normalizeDisputeBatchCode

fun summarizeDisputeExceptionsForReview(items: MutableList<DisputeException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveAuditReviewsFromException(items: MutableList<DisputeException>): MutableList<AuditReview> {
    val out = mutableListOf<AuditReview>()
    for (item in items) {
        val derived = newAuditReview(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreAuditReview(item: AuditReview): String = normalizeDisputeBatchCode(item.code)
