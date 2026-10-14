package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.linehaul.capacity.capacitymilestone.BatchedCapacityMilestone
import com.acme.transport.linehaul.capacity.capacitymilestone.normalizeCappedCapacityPolicyCode

fun summarizeBatchedCapacityMilestonesForTarget(items: MutableList<BatchedCapacityMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveTrailerTargetsFromMilestone(items: MutableList<BatchedCapacityMilestone>): MutableList<TrailerTarget> {
    val out = mutableListOf<TrailerTarget>()
    for (item in items) {
        val derived = newTrailerTarget(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeTrailerTarget(item: TrailerTarget): String = normalizeCappedCapacityPolicyCode(item.code)
