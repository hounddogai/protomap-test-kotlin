package com.acme.transport.lastmile.redelivery.digest

import com.acme.transport.lastmile.proofofdelivery.mapping.ProofOfDeliveryDigest
import com.acme.transport.lastmile.proofofdelivery.mapping.classifyProofOfDeliveryMapping

fun summarizeProofOfDeliveryDigestsForSlot(items: MutableList<ProofOfDeliveryDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveRedeliverySlotsFromDigest(items: MutableList<ProofOfDeliveryDigest>): MutableList<RedeliverySlot> {
    val out = mutableListOf<RedeliverySlot>()
    for (item in items) {
        val derived = newRedeliverySlot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeRedeliverySlot(item: RedeliverySlot): String = classifyProofOfDeliveryMapping(item.effectiveTotal())
