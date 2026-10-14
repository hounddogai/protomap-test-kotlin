package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.lastmile.proofofdelivery.segment.ProofOfDeliveryTemplate
import com.acme.transport.lastmile.proofofdelivery.segment.proofOfDeliveryTemplateWeight

fun summarizeProofOfDeliveryTemplatesForPolicy(items: MutableList<ProofOfDeliveryTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveProofOfDeliveryPoliciesFromTemplate(items: MutableList<ProofOfDeliveryTemplate>): MutableList<ProofOfDeliveryPolicy> {
    val out = mutableListOf<ProofOfDeliveryPolicy>()
    for (item in items) {
        val derived = newProofOfDeliveryPolicy(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitProofOfDeliveryPolicy(item: ProofOfDeliveryPolicy): Double = proofOfDeliveryTemplateWeight(6, true)
