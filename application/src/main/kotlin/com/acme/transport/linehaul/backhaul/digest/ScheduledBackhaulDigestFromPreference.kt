package com.acme.transport.linehaul.backhaul.digest

import com.acme.transport.linehaul.capacity.capacitypreference.WeightedCapacityPreference
import com.acme.transport.linehaul.capacity.capacitypreference.weightedCapacityPreferenceBucket

fun summarizeWeightedCapacityPreferencesForDigest(items: MutableList<WeightedCapacityPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveScheduledBackhaulDigestsFromPreference(items: MutableList<WeightedCapacityPreference>): MutableList<ScheduledBackhaulDigest> {
    val out = mutableListOf<ScheduledBackhaulDigest>()
    for (item in items) {
        val derived = newScheduledBackhaulDigest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateScheduledBackhaulDigest(item: ScheduledBackhaulDigest): String {
    return weightedCapacityPreferenceBucket(item.effectiveValue())
}
