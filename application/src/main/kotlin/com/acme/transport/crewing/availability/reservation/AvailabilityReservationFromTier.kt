package com.acme.transport.crewing.availability.reservation

import com.acme.transport.crewing.availability.availabilitytier.PendingAvailabilityTier
import com.acme.transport.crewing.availability.availabilitytier.pendingAvailabilityTierBucket

fun summarizePendingAvailabilityTiersForReservation(items: MutableList<PendingAvailabilityTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAvailabilityReservationsFromTier(items: MutableList<PendingAvailabilityTier>): MutableList<AvailabilityReservation> {
    val out = mutableListOf<AvailabilityReservation>()
    for (item in items) {
        val derived = newAvailabilityReservation(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleAvailabilityReservation(item: AvailabilityReservation): String {
    return pendingAvailabilityTierBucket(item.effectiveValue())
}
