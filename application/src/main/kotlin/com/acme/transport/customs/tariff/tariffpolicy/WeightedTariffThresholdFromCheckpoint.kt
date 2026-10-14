package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.customs.clearance.checkpoint.ManualClearanceCheckpoint
import com.acme.transport.customs.clearance.checkpoint.manualClearanceCheckpointBucket

fun summarizeManualClearanceCheckpointsForThreshold(items: MutableList<ManualClearanceCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveWeightedTariffThresholdsFromCheckpoint(items: MutableList<ManualClearanceCheckpoint>): MutableList<WeightedTariffThreshold> {
    val out = mutableListOf<WeightedTariffThreshold>()
    for (item in items) {
        val derived = newWeightedTariffThreshold(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyWeightedTariffThreshold(item: WeightedTariffThreshold): String {
    return manualClearanceCheckpointBucket(item.adjustedExposure())
}
