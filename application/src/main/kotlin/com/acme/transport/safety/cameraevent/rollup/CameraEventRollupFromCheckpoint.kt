package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.safety.coaching.template.ManualCoachingCheckpoint
import com.acme.transport.safety.coaching.template.classifyManualCoachingCheckpoint

fun summarizeManualCoachingCheckpointsForRollup(items: MutableList<ManualCoachingCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveCameraEventRollupsFromCheckpoint(items: MutableList<ManualCoachingCheckpoint>): MutableList<CameraEventRollup> {
    val out = mutableListOf<CameraEventRollup>()
    for (item in items) {
        val derived = newCameraEventRollup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileCameraEventRollup(item: CameraEventRollup): String {
    return classifyManualCoachingCheckpoint(item.projectedTotal())
}
