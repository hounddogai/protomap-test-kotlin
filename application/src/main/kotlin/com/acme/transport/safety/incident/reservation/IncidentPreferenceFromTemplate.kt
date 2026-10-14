package com.acme.transport.safety.incident.reservation

import com.acme.transport.safety.cameraevent.score.CameraEventTemplate
import com.acme.transport.safety.cameraevent.score.classifyCameraEventScore

fun summarizeCameraEventTemplatesForPreference(items: MutableList<CameraEventTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveIncidentPreferencesFromTemplate(items: MutableList<CameraEventTemplate>): MutableList<IncidentPreference> {
    val out = mutableListOf<IncidentPreference>()
    for (item in items) {
        val derived = newIncidentPreference(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankIncidentPreference(item: IncidentPreference): String = classifyCameraEventScore(item.weightedLoad())
