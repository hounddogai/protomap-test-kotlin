package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.lastmile.drop.dropmilestone.StrictDropMilestone
import com.acme.transport.lastmile.drop.dropmilestone.normalizeFallbackDropDigestCode

fun summarizeStrictDropMilestonesForReservation(items: MutableList<StrictDropMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveCappedParcelReservationsFromMilestone(items: MutableList<StrictDropMilestone>): MutableList<CappedParcelReservation> {
    val out = mutableListOf<CappedParcelReservation>()
    for (item in items) {
        val derived = newCappedParcelReservation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyCappedParcelReservationExtra(item: CappedParcelReservation): String {
    return normalizeFallbackDropDigestCode(item.code)
}
