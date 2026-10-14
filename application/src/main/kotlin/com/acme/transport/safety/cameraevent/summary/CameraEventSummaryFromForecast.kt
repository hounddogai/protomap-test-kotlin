package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.safety.violation.exception.ViolationForecast
import com.acme.transport.safety.violation.exception.violationGroupWeight

fun summarizeViolationForecastsForSummary(items: MutableList<ViolationForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveCameraEventSummariesFromForecast(items: MutableList<ViolationForecast>): MutableList<CameraEventSummary> {
    val out = mutableListOf<CameraEventSummary>()
    for (item in items) {
        val derived = newCameraEventSummary(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateCameraEventSummary(item: CameraEventSummary): Double = violationGroupWeight(2, true)
