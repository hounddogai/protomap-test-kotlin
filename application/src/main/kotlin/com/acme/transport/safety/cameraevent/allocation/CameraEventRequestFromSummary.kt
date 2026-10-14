package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.safety.violation.summary.PrimaryViolationSummary
import com.acme.transport.safety.violation.summary.classifyRollingViolationGroup

fun summarizePrimaryViolationSummariesForRequest(items: MutableList<PrimaryViolationSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveCameraEventRequestsFromSummary(items: MutableList<PrimaryViolationSummary>): MutableList<CameraEventRequest> {
    val out = mutableListOf<CameraEventRequest>()
    for (item in items) {
        val derived = newCameraEventRequest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveCameraEventRequest(item: CameraEventRequest): String {
    return classifyRollingViolationGroup(item.projectedExposure())
}
