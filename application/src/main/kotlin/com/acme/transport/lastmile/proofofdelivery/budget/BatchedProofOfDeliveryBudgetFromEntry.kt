package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.lastmile.parcel.parcelbaseline.CappedParcelEntry
import com.acme.transport.lastmile.parcel.parcelbaseline.tieredParcelBaselineBucket

fun summarizeCappedParcelEntriesForBudget(items: MutableList<CappedParcelEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveBatchedProofOfDeliveryBudgetsFromEntry(items: MutableList<CappedParcelEntry>): MutableList<BatchedProofOfDeliveryBudget> {
    val out = mutableListOf<BatchedProofOfDeliveryBudget>()
    for (item in items) {
        val derived = newBatchedProofOfDeliveryBudget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeBatchedProofOfDeliveryBudget(item: BatchedProofOfDeliveryBudget): String {
    return tieredParcelBaselineBucket(item.netExposure())
}
