package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.safety.nearmiss.slot.NearMissReservation
import com.acme.transport.safety.nearmiss.slot.nearMissSlotWeight

fun summarizeNearMissReservationsForRequest(items: MutableList<NearMissReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveIncidentRequestsFromReservation(items: MutableList<NearMissReservation>): MutableList<IncidentRequest> {
    val out = mutableListOf<IncidentRequest>()
    for (item in items) {
        val derived = newIncidentRequest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateIncidentRequest(item: IncidentRequest): Double = nearMissSlotWeight(8, true)
