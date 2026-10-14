package com.acme.transport.safety.cameraevent.eventbatch

import com.acme.transport.safety.scorecard.window.PrimaryScorecardSegment
import com.acme.transport.safety.scorecard.window.classifyPrimaryScorecardSegment

fun summarizePrimaryScorecardSegmentsForBatch(items: MutableList<PrimaryScorecardSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedCameraEventBatchesFromSegment(items: MutableList<PrimaryScorecardSegment>): MutableList<BatchedCameraEventBatch> {
    val out = mutableListOf<BatchedCameraEventBatch>()
    for (item in items) {
        val derived = newBatchedCameraEventBatch(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateBatchedCameraEventBatch(item: BatchedCameraEventBatch): String {
    return classifyPrimaryScorecardSegment(item.weightedTotal())
}
