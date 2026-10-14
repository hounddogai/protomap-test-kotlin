package com.acme.transport.safety.violation.adjustment

import com.acme.transport.safety.incident.incidentsegment.IncidentRequest
import com.acme.transport.safety.incident.incidentsegment.incidentRequestWeight

fun summarizeIncidentRequestsForAdjustment(items: MutableList<IncidentRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveViolationAdjustmentsFromRequest(items: MutableList<IncidentRequest>): MutableList<ViolationAdjustment> {
    val out = mutableListOf<ViolationAdjustment>()
    for (item in items) {
        val derived = newViolationAdjustment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeViolationAdjustment(item: ViolationAdjustment): Double = incidentRequestWeight(1, true)
