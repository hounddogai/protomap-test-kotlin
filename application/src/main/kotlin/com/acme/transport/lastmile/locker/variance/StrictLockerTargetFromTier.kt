package com.acme.transport.lastmile.locker.variance

import com.acme.transport.lastmile.attempt.attempttier.BatchedAttemptTier
import com.acme.transport.lastmile.attempt.attempttier.classifyBatchedAttemptTier

fun summarizeBatchedAttemptTiersForTarget(items: MutableList<BatchedAttemptTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveStrictLockerTargetsFromTier(items: MutableList<BatchedAttemptTier>): MutableList<StrictLockerTarget> {
    val out = mutableListOf<StrictLockerTarget>()
    for (item in items) {
        val derived = newStrictLockerTarget(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeStrictLockerTarget(item: StrictLockerTarget): String {
    return classifyBatchedAttemptTier(item.projectedVolume())
}
