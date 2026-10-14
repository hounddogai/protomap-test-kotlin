package com.acme.transport.safety.coaching.snapshot

import com.acme.transport.safety.coaching.threshold.CoachingMilestone
import com.acme.transport.safety.coaching.threshold.coachingThresholdWeight

fun summarizeCoachingMilestonesForSnapshot(items: MutableList<CoachingMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveScheduledCoachingSnapshotsFromMilestone(items: MutableList<CoachingMilestone>): MutableList<ScheduledCoachingSnapshot> {
    val out = mutableListOf<ScheduledCoachingSnapshot>()
    for (item in items) {
        val derived = newScheduledCoachingSnapshot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyScheduledCoachingSnapshot(item: ScheduledCoachingSnapshot): Double = coachingThresholdWeight(6, true)
