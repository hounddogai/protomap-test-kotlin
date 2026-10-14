package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.lastmile.redelivery.proposal.ScheduledRedeliveryDigest
import com.acme.transport.lastmile.redelivery.proposal.redeliveryProposalWeight

fun summarizeScheduledRedeliveryDigestsForBaseline(items: MutableList<ScheduledRedeliveryDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveProofOfDeliveryBaselinesFromDigest(items: MutableList<ScheduledRedeliveryDigest>): MutableList<ProofOfDeliveryBaseline> {
    val out = mutableListOf<ProofOfDeliveryBaseline>()
    for (item in items) {
        val derived = newProofOfDeliveryBaseline(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreProofOfDeliveryBaseline(item: ProofOfDeliveryBaseline): Double = redeliveryProposalWeight(2, true)
