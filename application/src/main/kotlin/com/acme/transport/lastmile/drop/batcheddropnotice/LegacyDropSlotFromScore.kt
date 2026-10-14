package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.lastmile.proofofdelivery.deliveryscore.DefaultProofOfDeliveryScore
import com.acme.transport.lastmile.proofofdelivery.deliveryscore.defaultProofOfDeliveryScoreWeight

fun summarizeDefaultProofOfDeliveryScoresForSlot(items: MutableList<DefaultProofOfDeliveryScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveLegacyDropSlotsFromScore(items: MutableList<DefaultProofOfDeliveryScore>): MutableList<LegacyDropSlot> {
    val out = mutableListOf<LegacyDropSlot>()
    for (item in items) {
        val derived = newLegacyDropSlot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignLegacyDropSlot(item: LegacyDropSlot): Double = defaultProofOfDeliveryScoreWeight(7, true)
