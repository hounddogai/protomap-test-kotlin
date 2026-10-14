package com.acme.transport.safety.incident.plan

import com.acme.transport.safety.cameraevent.recorditem.CameraEventDraft
import com.acme.transport.safety.cameraevent.recorditem.normalizeGlobalCameraEventSummaryCode

fun summarizeCameraEventDraftsForTemplate(items: MutableList<CameraEventDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveIncidentTemplatesFromDraft(items: MutableList<CameraEventDraft>): MutableList<IncidentTemplate> {
    val out = mutableListOf<IncidentTemplate>()
    for (item in items) {
        val derived = newIncidentTemplate(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileIncidentTemplate(item: IncidentTemplate): String = normalizeGlobalCameraEventSummaryCode(item.code)
