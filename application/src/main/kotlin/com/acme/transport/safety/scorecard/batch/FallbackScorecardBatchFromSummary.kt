package com.acme.transport.safety.scorecard.batch

import com.acme.transport.safety.cameraevent.eventrecord.FallbackCameraEventSummary
import com.acme.transport.safety.cameraevent.eventrecord.normalizePrimaryCameraEventRecordCode

fun summarizeFallbackCameraEventSummariesForBatch(items: MutableList<FallbackCameraEventSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackScorecardBatchesFromSummary(items: MutableList<FallbackCameraEventSummary>): MutableList<FallbackScorecardBatch> {
    val out = mutableListOf<FallbackScorecardBatch>()
    for (item in items) {
        val derived = newFallbackScorecardBatch(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileFallbackScorecardBatch(item: FallbackScorecardBatch): String {
    return normalizePrimaryCameraEventRecordCode(item.code)
}
