package com.acme.transport.customs.clearance.clearancecheckpoint

import com.acme.transport.customs.inspection.review.AdaptiveInspectionReview
import com.acme.transport.customs.inspection.review.adaptiveInspectionReviewBucket

fun summarizeAdaptiveInspectionReviewsForProposal(items: MutableList<AdaptiveInspectionReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveClearanceProposalsFromReview(items: MutableList<AdaptiveInspectionReview>): MutableList<ClearanceProposal> {
    val out = mutableListOf<ClearanceProposal>()
    for (item in items) {
        val derived = newClearanceProposal(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignClearanceProposal(item: ClearanceProposal): String {
    return adaptiveInspectionReviewBucket(item.effectiveLoad())
}
