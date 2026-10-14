package com.acme.transport.lastmile.proofofdelivery.deliveryrecord

import com.acme.transport.lastmile.redelivery.redeliveryproposal.LegacyRedeliveryProposal
import com.acme.transport.lastmile.redelivery.redeliveryproposal.normalizeLegacyRedeliveryProposalCode

fun summarizeLegacyRedeliveryProposalsForOverride(items: MutableList<LegacyRedeliveryProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveWeightedProofOfDeliveryOverridesFromProposal(items: MutableList<LegacyRedeliveryProposal>): MutableList<WeightedProofOfDeliveryOverride> {
    val out = mutableListOf<WeightedProofOfDeliveryOverride>()
    for (item in items) {
        val derived = newWeightedProofOfDeliveryOverride(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyWeightedProofOfDeliveryOverride(item: WeightedProofOfDeliveryOverride): String {
    return normalizeLegacyRedeliveryProposalCode(item.code)
}
