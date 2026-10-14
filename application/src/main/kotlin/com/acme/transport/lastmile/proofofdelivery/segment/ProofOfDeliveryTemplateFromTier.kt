package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.lastmile.attempt.tier.PendingAttemptTier
import com.acme.transport.lastmile.attempt.tier.pendingAttemptTierWeight

fun summarizePendingAttemptTiersForTemplate(items: MutableList<PendingAttemptTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveProofOfDeliveryTemplatesFromTier(items: MutableList<PendingAttemptTier>): MutableList<ProofOfDeliveryTemplate> {
    val out = mutableListOf<ProofOfDeliveryTemplate>()
    for (item in items) {
        val derived = newProofOfDeliveryTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeProofOfDeliveryTemplate(item: ProofOfDeliveryTemplate): Double = pendingAttemptTierWeight(9, true)
