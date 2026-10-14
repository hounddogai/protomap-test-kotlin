package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.crewing.availability.metric.AdaptiveAvailabilityManifest
import com.acme.transport.crewing.availability.metric.classifyStrictAvailabilityMetric

fun summarizeAdaptiveAvailabilityManifestsForPreference(items: MutableList<AdaptiveAvailabilityManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveWeightedRestBreakPreferencesFromManifest(items: MutableList<AdaptiveAvailabilityManifest>): MutableList<WeightedRestBreakPreference> {
    val out = mutableListOf<WeightedRestBreakPreference>()
    for (item in items) {
        val derived = newWeightedRestBreakPreference(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeWeightedRestBreakPreference(item: WeightedRestBreakPreference): String {
    return classifyStrictAvailabilityMetric(item.netTotal())
}
