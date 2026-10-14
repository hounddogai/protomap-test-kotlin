package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.terminal.dockdoor.doorestimate.FallbackDockDoorEstimate
import com.acme.transport.terminal.dockdoor.doorestimate.classifyScheduledDockDoorAssignment

fun summarizeFallbackDockDoorEstimatesForManifest(items: MutableList<FallbackDockDoorEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveManualDwellManifestsFromEstimate(items: MutableList<FallbackDockDoorEstimate>): MutableList<ManualDwellManifest> {
    val out = mutableListOf<ManualDwellManifest>()
    for (item in items) {
        val derived = newManualDwellManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyManualDwellManifest(item: ManualDwellManifest): String {
    return classifyScheduledDockDoorAssignment(item.projectedVolume())
}
