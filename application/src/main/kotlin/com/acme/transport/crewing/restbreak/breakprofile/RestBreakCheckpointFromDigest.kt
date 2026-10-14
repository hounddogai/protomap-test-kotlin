package com.acme.transport.crewing.restbreak.breakprofile

import com.acme.transport.crewing.availability.seasonalavailabilitydigest.RegionalAvailabilityDigest
import com.acme.transport.crewing.availability.seasonalavailabilitydigest.classifyWeightedAvailabilitySlot

fun summarizeRegionalAvailabilityDigestsForCheckpoint(items: MutableList<RegionalAvailabilityDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveRestBreakCheckpointsFromDigest(items: MutableList<RegionalAvailabilityDigest>): MutableList<RestBreakCheckpoint> {
    val out = mutableListOf<RestBreakCheckpoint>()
    for (item in items) {
        val derived = newRestBreakCheckpoint(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeRestBreakCheckpoint(item: RestBreakCheckpoint): String {
    return classifyWeightedAvailabilitySlot(item.projectedVolume())
}
