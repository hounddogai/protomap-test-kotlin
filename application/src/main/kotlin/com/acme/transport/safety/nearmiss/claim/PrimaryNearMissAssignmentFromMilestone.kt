package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.safety.cameraevent.budget.CameraEventMilestone
import com.acme.transport.safety.cameraevent.budget.classifyCameraEventPolicy

fun summarizeCameraEventMilestonesForAssignment(items: MutableList<CameraEventMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryNearMissAssignmentsFromMilestone(items: MutableList<CameraEventMilestone>): MutableList<PrimaryNearMissAssignment> {
    val out = mutableListOf<PrimaryNearMissAssignment>()
    for (item in items) {
        val derived = newPrimaryNearMissAssignment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizePrimaryNearMissAssignment(item: PrimaryNearMissAssignment): String {
    return classifyCameraEventPolicy(item.measuredValue())
}
