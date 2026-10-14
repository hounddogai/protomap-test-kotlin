package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.safety.cameraevent.eventslot.ManualCameraEventSlot
import com.acme.transport.safety.cameraevent.eventslot.classifyAdaptiveCameraEventRollup

fun summarizeManualCameraEventSlotsForBatch(items: MutableList<ManualCameraEventSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveManualCameraEventBatchesFromSlot(items: MutableList<ManualCameraEventSlot>): MutableList<ManualCameraEventBatch> {
    val out = mutableListOf<ManualCameraEventBatch>()
    for (item in items) {
        val derived = newManualCameraEventBatch(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectManualCameraEventBatchExtra(item: ManualCameraEventBatch): String {
    return classifyAdaptiveCameraEventRollup(item.projectedVolume())
}
