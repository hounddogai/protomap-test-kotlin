package com.acme.transport.network.zone.zoneagreement

import com.acme.transport.network.zone.review.GlobalZoneReview
import com.acme.transport.network.zone.review.manualZoneLedgerWeight

fun summarizeGlobalZoneReviewsForReservation(items: MutableList<GlobalZoneReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveAdaptiveZoneReservationsFromReview(items: MutableList<GlobalZoneReview>): MutableList<AdaptiveZoneReservation> {
    val out = mutableListOf<AdaptiveZoneReservation>()
    for (item in items) {
        val derived = newAdaptiveZoneReservation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeAdaptiveZoneReservation(item: AdaptiveZoneReservation): Double = manualZoneLedgerWeight(6, true)
