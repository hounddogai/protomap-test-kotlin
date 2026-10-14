package com.acme.transport.safety.coaching.estimate

import com.acme.transport.safety.cameraevent.cycle.CameraEventCycle
import com.acme.transport.safety.cameraevent.cycle.classifyCameraEventNotice

fun summarizeCameraEventCyclesForCheckpoint(items: MutableList<CameraEventCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveCoachingCheckpointsFromCycle(items: MutableList<CameraEventCycle>): MutableList<CoachingCheckpoint> {
    val out = mutableListOf<CoachingCheckpoint>()
    for (item in items) {
        val derived = newCoachingCheckpoint(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchCoachingCheckpoint(item: CoachingCheckpoint): String = classifyCameraEventNotice(item.netTotal())
