package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.safety.cameraevent.eventslot.ManualCameraEventSlot
import com.acme.transport.safety.cameraevent.eventslot.classifyAdaptiveCameraEventRollup

fun summarizeManualCameraEventSlotsForMilestone(items: MutableList<ManualCameraEventSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveBatchedIncidentMilestonesFromSlot(items: MutableList<ManualCameraEventSlot>): MutableList<BatchedIncidentMilestone> {
    val out = mutableListOf<BatchedIncidentMilestone>()
    for (item in items) {
        val derived = newBatchedIncidentMilestone(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeBatchedIncidentMilestone(item: BatchedIncidentMilestone): String {
    return classifyAdaptiveCameraEventRollup(item.netExposure())
}
