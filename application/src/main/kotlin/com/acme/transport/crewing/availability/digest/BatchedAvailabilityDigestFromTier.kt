package com.acme.transport.crewing.availability.digest

import com.acme.transport.crewing.availability.availabilityreservation.AdaptiveAvailabilityTier
import com.acme.transport.crewing.availability.availabilityreservation.normalizeCappedAvailabilityReservationCode

fun summarizeAdaptiveAvailabilityTiersForDigest(items: MutableList<AdaptiveAvailabilityTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveBatchedAvailabilityDigestsFromTier(items: MutableList<AdaptiveAvailabilityTier>): MutableList<BatchedAvailabilityDigest> {
    val out = mutableListOf<BatchedAvailabilityDigest>()
    for (item in items) {
        val derived = newBatchedAvailabilityDigest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitBatchedAvailabilityDigest(item: BatchedAvailabilityDigest): String {
    return normalizeCappedAvailabilityReservationCode(item.code)
}
