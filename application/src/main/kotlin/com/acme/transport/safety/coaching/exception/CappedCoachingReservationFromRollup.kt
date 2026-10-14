package com.acme.transport.safety.coaching.exception

import com.acme.transport.safety.cameraevent.eventslot.AdaptiveCameraEventRollup
import com.acme.transport.safety.cameraevent.eventslot.classifyAdaptiveCameraEventRollup

fun summarizeAdaptiveCameraEventRollupsForReservation(items: MutableList<AdaptiveCameraEventRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveCappedCoachingReservationsFromRollup(items: MutableList<AdaptiveCameraEventRollup>): MutableList<CappedCoachingReservation> {
    val out = mutableListOf<CappedCoachingReservation>()
    for (item in items) {
        val derived = newCappedCoachingReservation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateCappedCoachingReservation(item: CappedCoachingReservation): String {
    return classifyAdaptiveCameraEventRollup(item.projectedLoad())
}
