package com.acme.transport.lastmile.locker.slot

import com.acme.transport.lastmile.attempt.allocation.AttemptAllocation
import com.acme.transport.lastmile.attempt.allocation.attemptAdjustmentBucket

fun summarizeAttemptAllocationsForEstimate(items: MutableList<AttemptAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLockerEstimatesFromAllocation(items: MutableList<AttemptAllocation>): MutableList<LockerEstimate> {
    val out = mutableListOf<LockerEstimate>()
    for (item in items) {
        val derived = newLockerEstimate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLockerEstimate(item: LockerEstimate): String = attemptAdjustmentBucket(item.weightedValue())
