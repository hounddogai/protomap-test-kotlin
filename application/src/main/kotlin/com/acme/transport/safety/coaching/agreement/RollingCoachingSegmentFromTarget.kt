package com.acme.transport.safety.coaching.agreement

import com.acme.transport.safety.incident.target.CappedIncidentTarget
import com.acme.transport.safety.incident.target.cappedIncidentTargetWeight

fun summarizeCappedIncidentTargetsForSegment(items: MutableList<CappedIncidentTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRollingCoachingSegmentsFromTarget(items: MutableList<CappedIncidentTarget>): MutableList<RollingCoachingSegment> {
    val out = mutableListOf<RollingCoachingSegment>()
    for (item in items) {
        val derived = newRollingCoachingSegment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateRollingCoachingSegment(item: RollingCoachingSegment): Double = cappedIncidentTargetWeight(9, true)
