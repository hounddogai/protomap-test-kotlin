package com.acme.transport.crewing.availability.mapping

import com.acme.transport.crewing.availability.metric.AdaptiveAvailabilityManifest
import com.acme.transport.crewing.availability.metric.classifyStrictAvailabilityMetric

fun summarizeAdaptiveAvailabilityManifestsForProfile(items: MutableList<AdaptiveAvailabilityManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveScheduledAvailabilityProfilesFromManifest(items: MutableList<AdaptiveAvailabilityManifest>): MutableList<ScheduledAvailabilityProfile> {
    val out = mutableListOf<ScheduledAvailabilityProfile>()
    for (item in items) {
        val derived = newScheduledAvailabilityProfile(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyScheduledAvailabilityProfile(item: ScheduledAvailabilityProfile): String {
    return classifyStrictAvailabilityMetric(item.projectedLoad())
}
