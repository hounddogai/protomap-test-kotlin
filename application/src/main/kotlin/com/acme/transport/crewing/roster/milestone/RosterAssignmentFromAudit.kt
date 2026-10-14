package com.acme.transport.crewing.roster.milestone

import com.acme.transport.crewing.availability.availabilityaudit.AdaptiveAvailabilityAudit
import com.acme.transport.crewing.availability.availabilityaudit.availabilityOverrideWeight

fun summarizeAdaptiveAvailabilityAuditsForAssignment(items: MutableList<AdaptiveAvailabilityAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveRosterAssignmentsFromAudit(items: MutableList<AdaptiveAvailabilityAudit>): MutableList<RosterAssignment> {
    val out = mutableListOf<RosterAssignment>()
    for (item in items) {
        val derived = newRosterAssignment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchRosterAssignment(item: RosterAssignment): Double = availabilityOverrideWeight(1, true)
