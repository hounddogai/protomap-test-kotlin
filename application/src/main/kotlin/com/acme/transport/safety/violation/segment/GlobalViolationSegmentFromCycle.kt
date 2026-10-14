package com.acme.transport.safety.violation.segment

import com.acme.transport.safety.incident.digest.IncidentCycle
import com.acme.transport.safety.incident.digest.normalizeGlobalIncidentRequestCode

fun summarizeIncidentCyclesForSegment(items: MutableList<IncidentCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveGlobalViolationSegmentsFromCycle(items: MutableList<IncidentCycle>): MutableList<GlobalViolationSegment> {
    val out = mutableListOf<GlobalViolationSegment>()
    for (item in items) {
        val derived = newGlobalViolationSegment(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyGlobalViolationSegment(item: GlobalViolationSegment): String {
    return normalizeGlobalIncidentRequestCode(item.code)
}
