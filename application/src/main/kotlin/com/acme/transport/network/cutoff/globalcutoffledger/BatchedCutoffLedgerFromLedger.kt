package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.network.servicearea.threshold.ServiceAreaLedger
import com.acme.transport.network.servicearea.threshold.adaptiveServiceAreaThresholdBucket

fun summarizeServiceAreaLedgersForLedger(items: MutableList<ServiceAreaLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveBatchedCutoffLedgersFromLedger(items: MutableList<ServiceAreaLedger>): MutableList<BatchedCutoffLedger> {
    val out = mutableListOf<BatchedCutoffLedger>()
    for (item in items) {
        val derived = newBatchedCutoffLedger(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectBatchedCutoffLedger(item: BatchedCutoffLedger): String {
    return adaptiveServiceAreaThresholdBucket(item.netTotal())
}
