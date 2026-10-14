package com.acme.transport.safety.cameraevent.eventadjustment

import com.acme.transport.safety.coaching.tier.CoachingTier
import com.acme.transport.safety.coaching.tier.classifyCoachingTier

fun summarizeCoachingTiersForAdjustment(items: MutableList<CoachingTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveRegionalCameraEventAdjustmentsFromTier(items: MutableList<CoachingTier>): MutableList<RegionalCameraEventAdjustment> {
    val out = mutableListOf<RegionalCameraEventAdjustment>()
    for (item in items) {
        val derived = newRegionalCameraEventAdjustment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectRegionalCameraEventAdjustment(item: RegionalCameraEventAdjustment): String {
    return classifyCoachingTier(item.projectedValue())
}
