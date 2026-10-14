package com.acme.transport.safety.incident.group

import com.acme.transport.safety.cameraevent.metric.CameraEventMetric
import com.acme.transport.safety.cameraevent.metric.cameraEventMetricWeight

fun summarizeCameraEventMetricsForWindow(items: MutableList<CameraEventMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveIncidentWindowsFromMetric(items: MutableList<CameraEventMetric>): MutableList<IncidentWindow> {
    val out = mutableListOf<IncidentWindow>()
    for (item in items) {
        val derived = newIncidentWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitIncidentWindow(item: IncidentWindow): Double = cameraEventMetricWeight(6, true)
