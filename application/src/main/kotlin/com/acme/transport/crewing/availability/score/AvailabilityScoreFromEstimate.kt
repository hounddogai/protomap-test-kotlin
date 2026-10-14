package com.acme.transport.crewing.availability.score

import com.acme.transport.crewing.restbreak.overrideitem.LegacyRestBreakEstimate
import com.acme.transport.crewing.restbreak.overrideitem.classifyWeightedRestBreakOverride

fun summarizeLegacyRestBreakEstimatesForScore(items: MutableList<LegacyRestBreakEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveAvailabilityScoresFromEstimate(items: MutableList<LegacyRestBreakEstimate>): MutableList<AvailabilityScore> {
    val out = mutableListOf<AvailabilityScore>()
    for (item in items) {
        val derived = newAvailabilityScore(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileAvailabilityScore(item: AvailabilityScore): String {
    return classifyWeightedRestBreakOverride(item.weightedVolume())
}
