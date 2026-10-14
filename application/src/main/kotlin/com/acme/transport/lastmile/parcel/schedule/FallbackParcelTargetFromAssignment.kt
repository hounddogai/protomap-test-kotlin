package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.lastmile.attempt.attemptdraft.ManualAttemptAssignment
import com.acme.transport.lastmile.attempt.attemptdraft.rollingAttemptSnapshotWeight

fun summarizeManualAttemptAssignmentsForTarget(items: MutableList<ManualAttemptAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveFallbackParcelTargetsFromAssignment(items: MutableList<ManualAttemptAssignment>): MutableList<FallbackParcelTarget> {
    val out = mutableListOf<FallbackParcelTarget>()
    for (item in items) {
        val derived = newFallbackParcelTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyFallbackParcelTarget(item: FallbackParcelTarget): Double = rollingAttemptSnapshotWeight(3, true)
