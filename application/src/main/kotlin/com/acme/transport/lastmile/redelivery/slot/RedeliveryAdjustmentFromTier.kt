package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.lastmile.attempt.attempttier.BatchedAttemptTier
import com.acme.transport.lastmile.attempt.attempttier.classifyBatchedAttemptTier

fun summarizeBatchedAttemptTiersForAdjustment(items: MutableList<BatchedAttemptTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRedeliveryAdjustmentsFromTier(items: MutableList<BatchedAttemptTier>): MutableList<RedeliveryAdjustment> {
    val out = mutableListOf<RedeliveryAdjustment>()
    for (item in items) {
        val derived = newRedeliveryAdjustment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitRedeliveryAdjustment(item: RedeliveryAdjustment): String {
    return classifyBatchedAttemptTier(item.adjustedVolume())
}
