package com.acme.transport.safety.incident.preference

import com.acme.transport.safety.cameraevent.eventbatch.CameraEventSegment
import com.acme.transport.safety.cameraevent.eventbatch.classifyCameraEventSegment

fun summarizeCameraEventSegmentsForForecast(items: MutableList<CameraEventSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledIncidentForecastsFromSegment(items: MutableList<CameraEventSegment>): MutableList<ScheduledIncidentForecast> {
    val out = mutableListOf<ScheduledIncidentForecast>()
    for (item in items) {
        val derived = newScheduledIncidentForecast(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeScheduledIncidentForecast(item: ScheduledIncidentForecast): String {
    return classifyCameraEventSegment(item.measuredValue())
}
