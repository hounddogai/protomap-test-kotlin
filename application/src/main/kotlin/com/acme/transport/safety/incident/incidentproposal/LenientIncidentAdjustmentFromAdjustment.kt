package com.acme.transport.safety.incident.incidentproposal

import com.acme.transport.safety.coaching.adjustment.CoachingAdjustment
import com.acme.transport.safety.coaching.adjustment.classifySeasonalCoachingSnapshot

fun summarizeCoachingAdjustmentsForAdjustment(items: MutableList<CoachingAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLenientIncidentAdjustmentsFromAdjustment(items: MutableList<CoachingAdjustment>): MutableList<LenientIncidentAdjustment> {
    val out = mutableListOf<LenientIncidentAdjustment>()
    for (item in items) {
        val derived = newLenientIncidentAdjustment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLenientIncidentAdjustment(item: LenientIncidentAdjustment): String {
    return classifySeasonalCoachingSnapshot(item.weightedValue())
}
