package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.safety.nearmiss.preference.RegionalNearMissMilestone
import com.acme.transport.safety.nearmiss.preference.nearMissPreferenceWeight

fun summarizeRegionalNearMissMilestonesForMetric(items: MutableList<RegionalNearMissMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveLenientCameraEventMetricsFromMilestone(items: MutableList<RegionalNearMissMilestone>): MutableList<LenientCameraEventMetric> {
    val out = mutableListOf<LenientCameraEventMetric>()
    for (item in items) {
        val derived = newLenientCameraEventMetric(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyLenientCameraEventMetric(item: LenientCameraEventMetric): Double = nearMissPreferenceWeight(9, true)
