package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.safety.coaching.coachingsnapshot.CoachingWindow
import com.acme.transport.safety.coaching.coachingsnapshot.classifyCoachingForecast

fun summarizeCoachingWindowsForPolicy(items: MutableList<CoachingWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveIncidentPoliciesFromWindow(items: MutableList<CoachingWindow>): MutableList<IncidentPolicy> {
    val out = mutableListOf<IncidentPolicy>()
    for (item in items) {
        val derived = newIncidentPolicy(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileIncidentPolicy(item: IncidentPolicy): String = classifyCoachingForecast(item.effectiveValue())
