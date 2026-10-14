package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.lastmile.parcel.scheduledparcelreservation.SeasonalParcelReservation
import com.acme.transport.lastmile.parcel.scheduledparcelreservation.seasonalParcelReservationBucket

fun summarizeSeasonalParcelReservationsForManifest(items: MutableList<SeasonalParcelReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveAdaptiveAttemptManifestsFromReservation(items: MutableList<SeasonalParcelReservation>): MutableList<AdaptiveAttemptManifest> {
    val out = mutableListOf<AdaptiveAttemptManifest>()
    for (item in items) {
        val derived = newAdaptiveAttemptManifest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateAdaptiveAttemptManifest(item: AdaptiveAttemptManifest): String {
    return seasonalParcelReservationBucket(item.netExposure())
}
