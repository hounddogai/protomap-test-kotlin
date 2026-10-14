package com.acme.transport.safety.violation.policy

import com.acme.transport.safety.cameraevent.snapshot.ScheduledCameraEventSnapshot
import com.acme.transport.safety.cameraevent.snapshot.classifyScheduledCameraEventSnapshot

fun summarizeScheduledCameraEventSnapshotsForCycle(items: MutableList<ScheduledCameraEventSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveAdaptiveViolationCyclesFromSnapshot(items: MutableList<ScheduledCameraEventSnapshot>): MutableList<AdaptiveViolationCycle> {
    val out = mutableListOf<AdaptiveViolationCycle>()
    for (item in items) {
        val derived = newAdaptiveViolationCycle(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyAdaptiveViolationCycle(item: AdaptiveViolationCycle): String {
    return classifyScheduledCameraEventSnapshot(item.netExposure())
}
