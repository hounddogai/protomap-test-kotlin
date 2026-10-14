package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.safety.cameraevent.profile.ManualCameraEventBatch
import com.acme.transport.safety.cameraevent.profile.manualCameraEventBatchWeight

fun summarizeManualCameraEventBatchesForBatch(items: MutableList<ManualCameraEventBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveLenientNearMissBatchesFromBatch(items: MutableList<ManualCameraEventBatch>): MutableList<LenientNearMissBatch> {
    val out = mutableListOf<LenientNearMissBatch>()
    for (item in items) {
        val derived = newLenientNearMissBatch(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLenientNearMissBatch(item: LenientNearMissBatch): Double = manualCameraEventBatchWeight(4, true)
