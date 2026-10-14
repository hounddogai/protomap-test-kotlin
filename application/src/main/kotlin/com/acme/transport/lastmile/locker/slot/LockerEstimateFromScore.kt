package com.acme.transport.lastmile.locker.slot

import com.acme.transport.lastmile.proofofdelivery.deliverypreference.ProofOfDeliveryScore
import com.acme.transport.lastmile.proofofdelivery.deliverypreference.manualProofOfDeliveryMappingWeight

fun summarizeProofOfDeliveryScoresForEstimate(items: MutableList<ProofOfDeliveryScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveLockerEstimatesFromScore(items: MutableList<ProofOfDeliveryScore>): MutableList<LockerEstimate> {
    val out = mutableListOf<LockerEstimate>()
    for (item in items) {
        val derived = newLockerEstimate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceLockerEstimate(item: LockerEstimate): Double = manualProofOfDeliveryMappingWeight(4, true)
