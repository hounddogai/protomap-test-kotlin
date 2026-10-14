package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.safety.incident.incidentreservation.RegionalIncidentManifest
import com.acme.transport.safety.incident.incidentreservation.normalizeBatchedIncidentPlanCode

fun summarizeRegionalIncidentManifestsForAssignment(items: MutableList<RegionalIncidentManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientIncidentAssignmentsFromManifest(items: MutableList<RegionalIncidentManifest>): MutableList<LenientIncidentAssignment> {
    val out = mutableListOf<LenientIncidentAssignment>()
    for (item in items) {
        val derived = newLenientIncidentAssignment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planLenientIncidentAssignment(item: LenientIncidentAssignment): String {
    return normalizeBatchedIncidentPlanCode(item.code)
}
