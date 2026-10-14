package com.acme.transport.customs.clearance.rule

import com.acme.transport.customs.clearance.clearancerequest.ClearanceDigest
import com.acme.transport.customs.clearance.clearancerequest.primaryClearanceRequestBucket

fun summarizeClearanceDigestsForBatch(items: MutableList<ClearanceDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveClearanceBatchesFromDigest(items: MutableList<ClearanceDigest>): MutableList<ClearanceBatch> {
    val out = mutableListOf<ClearanceBatch>()
    for (item in items) {
        val derived = newClearanceBatch(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectClearanceBatch(item: ClearanceBatch): String = primaryClearanceRequestBucket(item.adjustedValue())
