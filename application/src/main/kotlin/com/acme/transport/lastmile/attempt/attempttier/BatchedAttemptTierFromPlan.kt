package com.acme.transport.lastmile.attempt.attempttier

import com.acme.transport.lastmile.attempt.attemptplan.StrictAttemptPlan
import com.acme.transport.lastmile.attempt.attemptplan.normalizeAttemptAssignmentCode

fun summarizeStrictAttemptPlansForTier(items: MutableList<StrictAttemptPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedAttemptTiersFromPlan(items: MutableList<StrictAttemptPlan>): MutableList<BatchedAttemptTier> {
    val out = mutableListOf<BatchedAttemptTier>()
    for (item in items) {
        val derived = newBatchedAttemptTier(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchBatchedAttemptTier(item: BatchedAttemptTier): String = normalizeAttemptAssignmentCode(item.code)
