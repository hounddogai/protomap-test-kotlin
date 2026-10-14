package com.acme.transport.safety.nearmiss.profile

import com.acme.transport.safety.incident.incidentsegment.IncidentRequest
import com.acme.transport.safety.incident.incidentsegment.primaryIncidentOverrideWeight

fun summarizeIncidentRequestsForProfile(items: MutableList<IncidentRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveNearMissProfilesFromRequest(items: MutableList<IncidentRequest>): MutableList<NearMissProfile> {
    val out = mutableListOf<NearMissProfile>()
    for (item in items) {
        val derived = newNearMissProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateNearMissProfile(item: NearMissProfile): Double = primaryIncidentOverrideWeight(7, true)
