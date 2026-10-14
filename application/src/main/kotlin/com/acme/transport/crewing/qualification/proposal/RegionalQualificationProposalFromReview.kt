package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.crewing.qualification.review.QualificationReview
import com.acme.transport.crewing.qualification.review.seasonalQualificationAllocationWeight

fun summarizeQualificationReviewsForProposal(items: MutableList<QualificationReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveRegionalQualificationProposalsFromReview(items: MutableList<QualificationReview>): MutableList<RegionalQualificationProposal> {
    val out = mutableListOf<RegionalQualificationProposal>()
    for (item in items) {
        val derived = newRegionalQualificationProposal(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleRegionalQualificationProposal(item: RegionalQualificationProposal): Double {
    return seasonalQualificationAllocationWeight(7, true)
}
