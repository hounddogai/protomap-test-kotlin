package com.acme.transport.safety.incident.digest

import com.acme.transport.safety.violation.slot.ViolationSlot
import com.acme.transport.safety.violation.slot.classifyLegacyViolationBundle

fun summarizeViolationSlotsForCycle(items: MutableList<ViolationSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveIncidentCyclesFromSlot(items: MutableList<ViolationSlot>): MutableList<IncidentCycle> {
    val out = mutableListOf<IncidentCycle>()
    for (item in items) {
        val derived = newIncidentCycle(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchIncidentCycle(item: IncidentCycle): String = classifyLegacyViolationBundle(item.netVolume())
