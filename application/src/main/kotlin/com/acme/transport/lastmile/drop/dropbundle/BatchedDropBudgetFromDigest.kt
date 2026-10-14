package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.lastmile.parcel.window.PrimaryParcelDigest
import com.acme.transport.lastmile.parcel.window.normalizePrimaryParcelDigestCode

fun summarizePrimaryParcelDigestsForBudget(items: MutableList<PrimaryParcelDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveBatchedDropBudgetsFromDigest(items: MutableList<PrimaryParcelDigest>): MutableList<BatchedDropBudget> {
    val out = mutableListOf<BatchedDropBudget>()
    for (item in items) {
        val derived = newBatchedDropBudget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankBatchedDropBudget(item: BatchedDropBudget): String = normalizePrimaryParcelDigestCode(item.code)
