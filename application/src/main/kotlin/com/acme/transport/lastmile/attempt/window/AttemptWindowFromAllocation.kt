package com.acme.transport.lastmile.attempt.window

import com.acme.transport.lastmile.locker.lockertier.ScheduledLockerAllocation
import com.acme.transport.lastmile.locker.lockertier.strictLockerPlanWeight

fun summarizeScheduledLockerAllocationsForWindow(items: MutableList<ScheduledLockerAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveAttemptWindowsFromAllocation(items: MutableList<ScheduledLockerAllocation>): MutableList<AttemptWindow> {
    val out = mutableListOf<AttemptWindow>()
    for (item in items) {
        val derived = newAttemptWindow(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeAttemptWindow(item: AttemptWindow): Double = strictLockerPlanWeight(8, true)
