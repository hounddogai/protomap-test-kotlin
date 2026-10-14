package com.acme.transport.lastmile.parcel.slot

import com.acme.transport.lastmile.attempt.tier.ManualAttemptEstimate
import com.acme.transport.lastmile.attempt.tier.globalAttemptRecordWeight

fun summarizeManualAttemptEstimatesForVariance(items: MutableList<ManualAttemptEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveBatchedParcelVariancesFromEstimate(items: MutableList<ManualAttemptEstimate>): MutableList<BatchedParcelVariance> {
    val out = mutableListOf<BatchedParcelVariance>()
    for (item in items) {
        val derived = newBatchedParcelVariance(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeBatchedParcelVariance(item: BatchedParcelVariance): Double = globalAttemptRecordWeight(8, true)
