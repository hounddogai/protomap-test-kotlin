package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.freightpay.audit.review.AuditReview
import com.acme.transport.freightpay.audit.review.classifyAuditRecord

fun summarizeAuditReviewsForProposal(items: MutableList<AuditReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveAuditProposalsFromReview(items: MutableList<AuditReview>): MutableList<AuditProposal> {
    val out = mutableListOf<AuditProposal>()
    for (item in items) {
        val derived = newAuditProposal(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planAuditProposal(item: AuditProposal): String = classifyAuditRecord(item.adjustedExposure())
