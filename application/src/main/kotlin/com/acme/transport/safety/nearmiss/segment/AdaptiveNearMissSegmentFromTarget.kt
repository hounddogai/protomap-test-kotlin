package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.safety.incident.incidenttarget.LenientIncidentTarget
import com.acme.transport.safety.incident.incidenttarget.rollingIncidentBaselineWeight

fun summarizeLenientIncidentTargetsForSegment(items: MutableList<LenientIncidentTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveAdaptiveNearMissSegmentsFromTarget(items: MutableList<LenientIncidentTarget>): MutableList<AdaptiveNearMissSegment> {
    val out = mutableListOf<AdaptiveNearMissSegment>()
    for (item in items) {
        val derived = newAdaptiveNearMissSegment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreAdaptiveNearMissSegment(item: AdaptiveNearMissSegment): Double = rollingIncidentBaselineWeight(3, true)
