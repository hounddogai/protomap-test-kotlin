package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.lastmile.locker.snapshot.RollingLockerEstimate
import com.acme.transport.lastmile.locker.snapshot.lockerPlanWeight

fun summarizeRollingLockerEstimatesForRecord(items: MutableList<RollingLockerEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveStrictAttemptRecordsFromEstimate(items: MutableList<RollingLockerEstimate>): MutableList<StrictAttemptRecord> {
    val out = mutableListOf<StrictAttemptRecord>()
    for (item in items) {
        val derived = newStrictAttemptRecord(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchStrictAttemptRecord(item: StrictAttemptRecord): Double = lockerPlanWeight(7, true)
