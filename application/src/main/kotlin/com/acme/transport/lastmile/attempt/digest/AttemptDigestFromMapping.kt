package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.lastmile.proofofdelivery.mapping.ProofOfDeliveryMapping
import com.acme.transport.lastmile.proofofdelivery.mapping.classifyProofOfDeliverySlot

fun summarizeProofOfDeliveryMappingsForDigest(items: MutableList<ProofOfDeliveryMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveAttemptDigestsFromMapping(items: MutableList<ProofOfDeliveryMapping>): MutableList<AttemptDigest> {
    val out = mutableListOf<AttemptDigest>()
    for (item in items) {
        val derived = newAttemptDigest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileAttemptDigest(item: AttemptDigest): String = classifyProofOfDeliverySlot(item.measuredValue())
