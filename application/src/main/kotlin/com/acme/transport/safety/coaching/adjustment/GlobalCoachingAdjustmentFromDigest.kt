package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.safety.violation.proposal.RegionalViolationDigest
import com.acme.transport.safety.violation.proposal.rollingViolationProposalWeight

fun summarizeRegionalViolationDigestsForAdjustment(items: MutableList<RegionalViolationDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveGlobalCoachingAdjustmentsFromDigest(items: MutableList<RegionalViolationDigest>): MutableList<GlobalCoachingAdjustment> {
    val out = mutableListOf<GlobalCoachingAdjustment>()
    for (item in items) {
        val derived = newGlobalCoachingAdjustment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateGlobalCoachingAdjustment(item: GlobalCoachingAdjustment): Double {
    return rollingViolationProposalWeight(5, true)
}
