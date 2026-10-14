package com.acme.transport.network.node.bundle

import com.acme.transport.network.transittime.threshold.TransitTimeThreshold
import com.acme.transport.network.transittime.threshold.scheduledTransitTimeEntryBucket

fun summarizeTransitTimeThresholdsForProfile(items: MutableList<TransitTimeThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveScheduledNodeProfilesFromThreshold(items: MutableList<TransitTimeThreshold>): MutableList<ScheduledNodeProfile> {
    val out = mutableListOf<ScheduledNodeProfile>()
    for (item in items) {
        val derived = newScheduledNodeProfile(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyScheduledNodeProfile(item: ScheduledNodeProfile): String {
    return scheduledTransitTimeEntryBucket(item.weightedExposure())
}
