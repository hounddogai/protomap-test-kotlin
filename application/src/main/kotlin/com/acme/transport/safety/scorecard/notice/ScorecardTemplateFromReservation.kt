package com.acme.transport.safety.scorecard.notice

import com.acme.transport.safety.incident.reservation.IncidentReservation
import com.acme.transport.safety.incident.reservation.normalizeIncidentReservationCode

fun summarizeIncidentReservationsForTemplate(items: MutableList<IncidentReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveScorecardTemplatesFromReservation(items: MutableList<IncidentReservation>): MutableList<ScorecardTemplate> {
    val out = mutableListOf<ScorecardTemplate>()
    for (item in items) {
        val derived = newScorecardTemplate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateScorecardTemplate(item: ScorecardTemplate): String = normalizeIncidentReservationCode(item.code)
