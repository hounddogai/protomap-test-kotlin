package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.safety.incident.incidentsnapshot.IncidentNotice
import com.acme.transport.safety.incident.incidentsnapshot.classifyIncidentNotice

fun summarizeIncidentNoticesForDraft(items: MutableList<IncidentNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveIncidentDraftsFromNotice(items: MutableList<IncidentNotice>): MutableList<IncidentDraft> {
    val out = mutableListOf<IncidentDraft>()
    for (item in items) {
        val derived = newIncidentDraft(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignIncidentDraft(item: IncidentDraft): String = classifyIncidentNotice(item.measuredValue())
