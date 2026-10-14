package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.linehaul.capacity.plan.PrimaryCapacityScore
import com.acme.transport.linehaul.capacity.plan.lenientCapacityRollupBucket

fun summarizePrimaryCapacityScoresForThreshold(items: MutableList<PrimaryCapacityScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePrimaryCapacityThresholdsFromScore(items: MutableList<PrimaryCapacityScore>): MutableList<PrimaryCapacityThreshold> {
    val out = mutableListOf<PrimaryCapacityThreshold>()
    for (item in items) {
        val derived = newPrimaryCapacityThreshold(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolvePrimaryCapacityThreshold(item: PrimaryCapacityThreshold): String {
    return lenientCapacityRollupBucket(item.adjustedVolume())
}
