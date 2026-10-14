package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.safety.scorecard.defaultscorecardledger.AdaptiveScorecardLedger
import com.acme.transport.safety.scorecard.defaultscorecardledger.classifyScorecardAdjustment

fun summarizeAdaptiveScorecardLedgersForBatch(items: MutableList<AdaptiveScorecardLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveManualCameraEventBatchesFromLedger(items: MutableList<AdaptiveScorecardLedger>): MutableList<ManualCameraEventBatch> {
    val out = mutableListOf<ManualCameraEventBatch>()
    for (item in items) {
        val derived = newManualCameraEventBatch(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectManualCameraEventBatch(item: ManualCameraEventBatch): String {
    return classifyScorecardAdjustment(item.projectedVolume())
}
