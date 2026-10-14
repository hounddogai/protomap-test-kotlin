package com.acme.transport.safety.cameraevent.metric

import com.acme.transport.safety.nearmiss.profile.NearMissProfile
import com.acme.transport.safety.nearmiss.profile.normalizeNearMissProfileCode

fun summarizeNearMissProfilesForReservation(items: MutableList<NearMissProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveFallbackCameraEventReservationsFromProfile(items: MutableList<NearMissProfile>): MutableList<FallbackCameraEventReservation> {
    val out = mutableListOf<FallbackCameraEventReservation>()
    for (item in items) {
        val derived = newFallbackCameraEventReservation(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planFallbackCameraEventReservation(item: FallbackCameraEventReservation): String {
    return normalizeNearMissProfileCode(item.code)
}
