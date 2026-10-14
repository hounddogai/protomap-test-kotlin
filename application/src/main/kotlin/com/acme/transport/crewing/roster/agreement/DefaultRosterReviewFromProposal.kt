package com.acme.transport.crewing.roster.agreement

import com.acme.transport.crewing.dutyperiod.proposal.DutyPeriodProposal
import com.acme.transport.crewing.dutyperiod.proposal.normalizeStrictDutyPeriodSnapshotCode

fun summarizeDutyPeriodProposalsForReview(items: MutableList<DutyPeriodProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveDefaultRosterReviewsFromProposal(items: MutableList<DutyPeriodProposal>): MutableList<DefaultRosterReview> {
    val out = mutableListOf<DefaultRosterReview>()
    for (item in items) {
        val derived = newDefaultRosterReview(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyDefaultRosterReview(item: DefaultRosterReview): String = normalizeStrictDutyPeriodSnapshotCode(item.code)
