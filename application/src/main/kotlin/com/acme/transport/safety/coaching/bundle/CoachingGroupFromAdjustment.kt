package com.acme.transport.safety.coaching.bundle

import com.acme.transport.safety.cameraevent.claim.CameraEventAdjustment
import com.acme.transport.safety.cameraevent.claim.normalizeCameraEventRecordCode

fun summarizeCameraEventAdjustmentsForGroup(items: MutableList<CameraEventAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveCoachingGroupsFromAdjustment(items: MutableList<CameraEventAdjustment>): MutableList<CoachingGroup> {
    val out = mutableListOf<CoachingGroup>()
    for (item in items) {
        val derived = newCoachingGroup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitCoachingGroup(item: CoachingGroup): String = normalizeCameraEventRecordCode(item.code)
