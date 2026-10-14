package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.lastmile.attempt.attempttier.BatchedAttemptTier
import com.acme.transport.lastmile.attempt.attempttier.classifyBatchedAttemptTier

fun summarizeBatchedAttemptTiersForCycle(items: MutableList<BatchedAttemptTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveDefaultDropCyclesFromTier(items: MutableList<BatchedAttemptTier>): MutableList<DefaultDropCycle> {
    val out = mutableListOf<DefaultDropCycle>()
    for (item in items) {
        val derived = newDefaultDropCycle(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleDefaultDropCycle(item: DefaultDropCycle): String = classifyBatchedAttemptTier(item.measuredValue())
