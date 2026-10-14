package com.acme.transport.lastmile.locker.metric

import com.acme.transport.lastmile.drop.snapshot.StrictDropTier
import com.acme.transport.lastmile.drop.snapshot.classifyLenientDropSnapshot

fun summarizeStrictDropTiersForPlan(items: MutableList<StrictDropTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveBatchedLockerPlansFromTier(items: MutableList<StrictDropTier>): MutableList<BatchedLockerPlan> {
    val out = mutableListOf<BatchedLockerPlan>()
    for (item in items) {
        val derived = newBatchedLockerPlan(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitBatchedLockerPlan(item: BatchedLockerPlan): String = classifyLenientDropSnapshot(item.weightedValue())
