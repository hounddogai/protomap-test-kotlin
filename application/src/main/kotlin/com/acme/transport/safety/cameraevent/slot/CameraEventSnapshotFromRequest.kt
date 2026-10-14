package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.safety.incident.incidentsnapshot.PendingIncidentRequest
import com.acme.transport.safety.incident.incidentsnapshot.normalizeAdaptiveIncidentProposalCode

fun summarizePendingIncidentRequestsForSnapshot(items: MutableList<PendingIncidentRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCameraEventSnapshotsFromRequest(items: MutableList<PendingIncidentRequest>): MutableList<CameraEventSnapshot> {
    val out = mutableListOf<CameraEventSnapshot>()
    for (item in items) {
        val derived = newCameraEventSnapshot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeCameraEventSnapshot(item: CameraEventSnapshot): String {
    return normalizeAdaptiveIncidentProposalCode(item.code)
}
