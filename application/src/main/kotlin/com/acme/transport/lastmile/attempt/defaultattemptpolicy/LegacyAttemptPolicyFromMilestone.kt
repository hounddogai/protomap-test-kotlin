package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest.DefaultProofOfDeliveryMilestone
import com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest.batchedProofOfDeliveryManifestWeight

fun summarizeDefaultProofOfDeliveryMilestonesForPolicy(items: MutableList<DefaultProofOfDeliveryMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyAttemptPoliciesFromMilestone(items: MutableList<DefaultProofOfDeliveryMilestone>): MutableList<LegacyAttemptPolicy> {
    val out = mutableListOf<LegacyAttemptPolicy>()
    for (item in items) {
        val derived = newLegacyAttemptPolicy(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankLegacyAttemptPolicy(item: LegacyAttemptPolicy): Double = batchedProofOfDeliveryManifestWeight(2, true)
