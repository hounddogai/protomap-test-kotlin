package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.lastmile.proofofdelivery.ledger.FallbackProofOfDeliveryLedger
import com.acme.transport.lastmile.proofofdelivery.ledger.classifyFallbackProofOfDeliveryLedger

fun summarizeFallbackProofOfDeliveryLedgersForReservation(items: MutableList<FallbackProofOfDeliveryLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryProofOfDeliveryReservationsFromLedger(items: MutableList<FallbackProofOfDeliveryLedger>): MutableList<PrimaryProofOfDeliveryReservation> {
    val out = mutableListOf<PrimaryProofOfDeliveryReservation>()
    for (item in items) {
        val derived = newPrimaryProofOfDeliveryReservation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyPrimaryProofOfDeliveryReservationExtra(item: PrimaryProofOfDeliveryReservation): String {
    return classifyFallbackProofOfDeliveryLedger(item.weightedValue())
}
