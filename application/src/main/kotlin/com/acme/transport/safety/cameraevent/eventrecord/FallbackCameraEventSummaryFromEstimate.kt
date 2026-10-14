package com.acme.transport.safety.cameraevent.eventrecord

import com.acme.transport.safety.nearmiss.missestimate.WeightedNearMissEstimate
import com.acme.transport.safety.nearmiss.missestimate.cappedNearMissRecordWeight

fun summarizeWeightedNearMissEstimatesForSummary(items: MutableList<WeightedNearMissEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveFallbackCameraEventSummariesFromEstimate(items: MutableList<WeightedNearMissEstimate>): MutableList<FallbackCameraEventSummary> {
    val out = mutableListOf<FallbackCameraEventSummary>()
    for (item in items) {
        val derived = newFallbackCameraEventSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectFallbackCameraEventSummary(item: FallbackCameraEventSummary): Double {
    return cappedNearMissRecordWeight(6, true)
}
