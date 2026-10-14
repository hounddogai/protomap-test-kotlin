package com.acme.transport.lastmile.redelivery.manifest

import com.acme.transport.lastmile.proofofdelivery.baseline.WeightedProofOfDeliveryReservation
import com.acme.transport.lastmile.proofofdelivery.baseline.regionalProofOfDeliveryBaselineWeight

fun summarizeWeightedProofOfDeliveryReservationsForSlot(items: MutableList<WeightedProofOfDeliveryReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun derivePendingRedeliverySlotsFromReservation(items: MutableList<WeightedProofOfDeliveryReservation>): MutableList<PendingRedeliverySlot> {
    val out = mutableListOf<PendingRedeliverySlot>()
    for (item in items) {
        val derived = newPendingRedeliverySlot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyPendingRedeliverySlot(item: PendingRedeliverySlot): Double {
    return regionalProofOfDeliveryBaselineWeight(6, true)
}
