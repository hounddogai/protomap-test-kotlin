package com.acme.transport.safety.incident.claim

import com.acme.transport.safety.cameraevent.budget.CameraEventMilestone
import com.acme.transport.safety.cameraevent.budget.normalizeCameraEventEntryCode

fun summarizeCameraEventMilestonesForClaim(items: MutableList<CameraEventMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictIncidentClaimsFromMilestone(items: MutableList<CameraEventMilestone>): MutableList<StrictIncidentClaim> {
    val out = mutableListOf<StrictIncidentClaim>()
    for (item in items) {
        val derived = newStrictIncidentClaim(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planStrictIncidentClaim(item: StrictIncidentClaim): String = normalizeCameraEventEntryCode(item.code)
