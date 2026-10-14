package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.linehaul.trailer.fallbacktrailermilestone.RegionalTrailerMilestone
import com.acme.transport.linehaul.trailer.fallbacktrailermilestone.normalizeTrailerPlanCode

fun summarizeRegionalTrailerMilestonesForOverride(items: MutableList<RegionalTrailerMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveManualTrailerOverridesFromMilestone(items: MutableList<RegionalTrailerMilestone>): MutableList<ManualTrailerOverride> {
    val out = mutableListOf<ManualTrailerOverride>()
    for (item in items) {
        val derived = newManualTrailerOverride(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveManualTrailerOverride(item: ManualTrailerOverride): String = normalizeTrailerPlanCode(item.code)
