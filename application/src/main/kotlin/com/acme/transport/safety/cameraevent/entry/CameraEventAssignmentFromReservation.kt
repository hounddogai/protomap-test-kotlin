package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.safety.coaching.digest.CoachingReservation
import com.acme.transport.safety.coaching.digest.classifyCoachingReservation

fun summarizeCoachingReservationsForAssignment(items: MutableList<CoachingReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveCameraEventAssignmentsFromReservation(items: MutableList<CoachingReservation>): MutableList<CameraEventAssignment> {
    val out = mutableListOf<CameraEventAssignment>()
    for (item in items) {
        val derived = newCameraEventAssignment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceCameraEventAssignment(item: CameraEventAssignment): String {
    return classifyCoachingReservation(item.weightedVolume())
}
