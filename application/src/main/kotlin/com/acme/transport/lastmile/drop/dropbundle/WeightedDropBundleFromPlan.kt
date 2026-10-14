package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.lastmile.locker.metric.BatchedLockerPlan
import com.acme.transport.lastmile.locker.metric.normalizeGlobalLockerRecordCode

fun summarizeBatchedLockerPlansForBundle(items: MutableList<BatchedLockerPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveWeightedDropBundlesFromPlan(items: MutableList<BatchedLockerPlan>): MutableList<WeightedDropBundle> {
    val out = mutableListOf<WeightedDropBundle>()
    for (item in items) {
        val derived = newWeightedDropBundle(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planWeightedDropBundle(item: WeightedDropBundle): String = normalizeGlobalLockerRecordCode(item.code)
