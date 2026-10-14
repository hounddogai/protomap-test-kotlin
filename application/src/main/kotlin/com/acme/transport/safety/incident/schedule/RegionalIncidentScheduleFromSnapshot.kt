package com.acme.transport.safety.incident.schedule

import com.acme.transport.safety.cameraevent.snapshot.RollingCameraEventSnapshot
import com.acme.transport.safety.cameraevent.snapshot.scheduledCameraEventSnapshotWeight

fun summarizeRollingCameraEventSnapshotsForSchedule(items: MutableList<RollingCameraEventSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRegionalIncidentSchedulesFromSnapshot(items: MutableList<RollingCameraEventSnapshot>): MutableList<RegionalIncidentSchedule> {
    val out = mutableListOf<RegionalIncidentSchedule>()
    for (item in items) {
        val derived = newRegionalIncidentSchedule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeRegionalIncidentSchedule(item: RegionalIncidentSchedule): Double {
    return scheduledCameraEventSnapshotWeight(2, true)
}
