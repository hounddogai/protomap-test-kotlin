package com.acme.transport.safety.cameraevent.metric

import com.acme.transport.safety.incident.incidentsegment.IncidentSlot
import com.acme.transport.safety.incident.incidentsegment.primaryIncidentOverrideWeight

fun summarizeIncidentSlotsForReservation(items: MutableList<IncidentSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveFallbackCameraEventReservationsFromSlot(items: MutableList<IncidentSlot>): MutableList<FallbackCameraEventReservation> {
    val out = mutableListOf<FallbackCameraEventReservation>()
    for (item in items) {
        val derived = newFallbackCameraEventReservation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchFallbackCameraEventReservation(item: FallbackCameraEventReservation): Double {
    return primaryIncidentOverrideWeight(9, true)
}
