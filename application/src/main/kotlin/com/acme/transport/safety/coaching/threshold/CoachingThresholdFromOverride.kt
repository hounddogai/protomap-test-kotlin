package com.acme.transport.safety.coaching.threshold

import com.acme.transport.safety.incident.baseline.IncidentOverride
import com.acme.transport.safety.incident.baseline.incidentBaselineWeight

fun summarizeIncidentOverridesForThreshold(items: MutableList<IncidentOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveCoachingThresholdsFromOverride(items: MutableList<IncidentOverride>): MutableList<CoachingThreshold> {
    val out = mutableListOf<CoachingThreshold>()
    for (item in items) {
        val derived = newCoachingThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyCoachingThreshold(item: CoachingThreshold): Double = incidentBaselineWeight(5, true)
