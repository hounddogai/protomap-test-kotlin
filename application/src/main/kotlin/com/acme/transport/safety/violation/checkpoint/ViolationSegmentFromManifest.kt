package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.safety.coaching.target.CoachingManifest
import com.acme.transport.safety.coaching.target.coachingTargetWeight

fun summarizeCoachingManifestsForSegment(items: MutableList<CoachingManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveViolationSegmentsFromManifest(items: MutableList<CoachingManifest>): MutableList<ViolationSegment> {
    val out = mutableListOf<ViolationSegment>()
    for (item in items) {
        val derived = newViolationSegment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeViolationSegment(item: ViolationSegment): Double = coachingTargetWeight(3, true)
