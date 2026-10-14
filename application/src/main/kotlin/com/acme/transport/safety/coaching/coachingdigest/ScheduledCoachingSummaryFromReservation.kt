package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.safety.incident.incidentreservation.LegacyIncidentReservation
import com.acme.transport.safety.incident.incidentreservation.normalizeBatchedIncidentPlanCode

fun summarizeLegacyIncidentReservationsForSummary(items: MutableList<LegacyIncidentReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveScheduledCoachingSummariesFromReservation(items: MutableList<LegacyIncidentReservation>): MutableList<ScheduledCoachingSummary> {
    val out = mutableListOf<ScheduledCoachingSummary>()
    for (item in items) {
        val derived = newScheduledCoachingSummary(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planScheduledCoachingSummary(item: ScheduledCoachingSummary): String {
    return normalizeBatchedIncidentPlanCode(item.code)
}
