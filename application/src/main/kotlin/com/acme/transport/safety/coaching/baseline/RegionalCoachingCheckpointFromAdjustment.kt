package com.acme.transport.safety.coaching.baseline

import com.acme.transport.safety.coaching.adjustment.CoachingAdjustment
import com.acme.transport.safety.coaching.adjustment.normalizeCoachingAdjustmentCode

fun summarizeCoachingAdjustmentsForCheckpoint(items: MutableList<CoachingAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveRegionalCoachingCheckpointsFromAdjustment(items: MutableList<CoachingAdjustment>): MutableList<RegionalCoachingCheckpoint> {
    val out = mutableListOf<RegionalCoachingCheckpoint>()
    for (item in items) {
        val derived = newRegionalCoachingCheckpoint(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyRegionalCoachingCheckpoint(item: RegionalCoachingCheckpoint): String {
    return normalizeCoachingAdjustmentCode(item.code)
}
