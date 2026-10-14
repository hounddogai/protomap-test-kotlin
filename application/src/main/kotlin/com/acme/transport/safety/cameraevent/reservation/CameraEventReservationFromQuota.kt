package com.acme.transport.safety.cameraevent.reservation

import com.acme.transport.safety.incident.baseline.IncidentQuota
import com.acme.transport.safety.incident.baseline.incidentQuotaBucket

fun summarizeIncidentQuotasForReservation(items: MutableList<IncidentQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveCameraEventReservationsFromQuota(items: MutableList<IncidentQuota>): MutableList<CameraEventReservation> {
    val out = mutableListOf<CameraEventReservation>()
    for (item in items) {
        val derived = newCameraEventReservation(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeCameraEventReservation(item: CameraEventReservation): String {
    return incidentQuotaBucket(item.effectiveLoad())
}
