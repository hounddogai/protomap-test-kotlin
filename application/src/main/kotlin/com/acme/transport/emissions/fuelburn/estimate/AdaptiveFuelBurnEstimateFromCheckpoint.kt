package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.emissions.idling.checkpoint.PrimaryIdlingCheckpoint
import com.acme.transport.emissions.idling.checkpoint.primaryIdlingCheckpointBucket

fun summarizePrimaryIdlingCheckpointsForEstimate(items: MutableList<PrimaryIdlingCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveAdaptiveFuelBurnEstimatesFromCheckpoint(items: MutableList<PrimaryIdlingCheckpoint>): MutableList<AdaptiveFuelBurnEstimate> {
    val out = mutableListOf<AdaptiveFuelBurnEstimate>()
    for (item in items) {
        val derived = newAdaptiveFuelBurnEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyAdaptiveFuelBurnEstimateExtra(item: AdaptiveFuelBurnEstimate): String {
    return primaryIdlingCheckpointBucket(item.effectiveVolume())
}
