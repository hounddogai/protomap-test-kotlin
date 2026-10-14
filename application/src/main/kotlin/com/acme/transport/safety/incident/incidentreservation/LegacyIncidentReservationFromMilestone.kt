package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.safety.violation.violationslot.StrictViolationMilestone
import com.acme.transport.safety.violation.violationslot.scheduledViolationTemplateWeight

fun summarizeStrictViolationMilestonesForReservation(items: MutableList<StrictViolationMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveLegacyIncidentReservationsFromMilestone(items: MutableList<StrictViolationMilestone>): MutableList<LegacyIncidentReservation> {
    val out = mutableListOf<LegacyIncidentReservation>()
    for (item in items) {
        val derived = newLegacyIncidentReservation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceLegacyIncidentReservation(item: LegacyIncidentReservation): Double {
    return scheduledViolationTemplateWeight(7, true)
}
