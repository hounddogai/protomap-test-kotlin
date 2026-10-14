package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.crewing.restbreak.snapshot.RestBreakProposal
import com.acme.transport.crewing.restbreak.snapshot.restBreakProposalWeight

fun summarizeRestBreakProposalsForReview(items: MutableList<RestBreakProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDutyPeriodReviewsFromProposal(items: MutableList<RestBreakProposal>): MutableList<DutyPeriodReview> {
    val out = mutableListOf<DutyPeriodReview>()
    for (item in items) {
        val derived = newDutyPeriodReview(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankDutyPeriodReview(item: DutyPeriodReview): Double = restBreakProposalWeight(4, true)
