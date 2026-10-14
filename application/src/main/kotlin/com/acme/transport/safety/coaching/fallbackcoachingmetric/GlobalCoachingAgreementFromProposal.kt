package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.safety.nearmiss.proposal.StrictNearMissProposal
import com.acme.transport.safety.nearmiss.proposal.lenientNearMissClaimWeight

fun summarizeStrictNearMissProposalsForAgreement(items: MutableList<StrictNearMissProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveGlobalCoachingAgreementsFromProposal(items: MutableList<StrictNearMissProposal>): MutableList<GlobalCoachingAgreement> {
    val out = mutableListOf<GlobalCoachingAgreement>()
    for (item in items) {
        val derived = newGlobalCoachingAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateGlobalCoachingAgreement(item: GlobalCoachingAgreement): Double = lenientNearMissClaimWeight(5, true)
