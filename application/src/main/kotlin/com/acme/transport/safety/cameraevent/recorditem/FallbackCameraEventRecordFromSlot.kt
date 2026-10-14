package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.safety.scorecard.tieredscorecardslot.LenientScorecardSlot
import com.acme.transport.safety.scorecard.tieredscorecardslot.classifyLenientScorecardSlot

fun summarizeLenientScorecardSlotsForRecord(items: MutableList<LenientScorecardSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveFallbackCameraEventRecordsFromSlot(items: MutableList<LenientScorecardSlot>): MutableList<FallbackCameraEventRecord> {
    val out = mutableListOf<FallbackCameraEventRecord>()
    for (item in items) {
        val derived = newFallbackCameraEventRecord(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateFallbackCameraEventRecord(item: FallbackCameraEventRecord): String {
    return classifyLenientScorecardSlot(item.adjustedVolume())
}
