package com.acme.transport.safety.incident.bundle

import com.acme.transport.safety.scorecard.schedule.DefaultScorecardAssignment
import com.acme.transport.safety.scorecard.schedule.normalizeDefaultScorecardAssignmentCode

fun summarizeDefaultScorecardAssignmentsForBundle(items: MutableList<DefaultScorecardAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveIncidentBundlesFromAssignment(items: MutableList<DefaultScorecardAssignment>): MutableList<IncidentBundle> {
    val out = mutableListOf<IncidentBundle>()
    for (item in items) {
        val derived = newIncidentBundle(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceIncidentBundle(item: IncidentBundle): String = normalizeDefaultScorecardAssignmentCode(item.code)
