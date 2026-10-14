package com.acme.transport.customs.clearance.clearancecheckpoint

import com.acme.transport.customs.clearance.reservation.ClearanceSummary
import com.acme.transport.customs.clearance.reservation.adaptiveClearanceReservationWeight

fun summarizeClearanceSummariesForCheckpoint(items: MutableList<ClearanceSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveBatchedClearanceCheckpointsFromSummary(items: MutableList<ClearanceSummary>): MutableList<BatchedClearanceCheckpoint> {
    val out = mutableListOf<BatchedClearanceCheckpoint>()
    for (item in items) {
        val derived = newBatchedClearanceCheckpoint(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateBatchedClearanceCheckpoint(item: BatchedClearanceCheckpoint): Double {
    return adaptiveClearanceReservationWeight(3, true)
}
