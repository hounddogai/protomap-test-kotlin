package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.lastmile.proofofdelivery.rollup.PrimaryProofOfDeliveryReservation
import com.acme.transport.lastmile.proofofdelivery.rollup.primaryProofOfDeliveryReservationBucket

fun summarizePrimaryProofOfDeliveryReservationsForReservation(items: MutableList<PrimaryProofOfDeliveryReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveStrictProofOfDeliveryReservationsFromReservation(items: MutableList<PrimaryProofOfDeliveryReservation>): MutableList<StrictProofOfDeliveryReservation> {
    val out = mutableListOf<StrictProofOfDeliveryReservation>()
    for (item in items) {
        val derived = newStrictProofOfDeliveryReservation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateStrictProofOfDeliveryReservation(item: StrictProofOfDeliveryReservation): String {
    return primaryProofOfDeliveryReservationBucket(item.measuredValue())
}
