package com.acme.transport.crewing.availability.profile

import com.acme.transport.crewing.roster.threshold.RosterThreshold
import com.acme.transport.crewing.roster.threshold.classifyPrimaryRosterDigest

fun summarizeRosterThresholdsForPreference(items: MutableList<RosterThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun derivePendingAvailabilityPreferencesFromThreshold(items: MutableList<RosterThreshold>): MutableList<PendingAvailabilityPreference> {
    val out = mutableListOf<PendingAvailabilityPreference>()
    for (item in items) {
        val derived = newPendingAvailabilityPreference(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyPendingAvailabilityPreference(item: PendingAvailabilityPreference): String {
    return classifyPrimaryRosterDigest(item.netLoad())
}
