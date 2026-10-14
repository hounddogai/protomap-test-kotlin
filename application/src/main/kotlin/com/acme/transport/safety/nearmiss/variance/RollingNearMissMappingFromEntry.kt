package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.safety.violation.entry.ViolationEntry
import com.acme.transport.safety.violation.entry.classifyManualViolationWindow

fun summarizeViolationEntriesForMapping(items: MutableList<ViolationEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingNearMissMappingsFromEntry(items: MutableList<ViolationEntry>): MutableList<RollingNearMissMapping> {
    val out = mutableListOf<RollingNearMissMapping>()
    for (item in items) {
        val derived = newRollingNearMissMapping(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankRollingNearMissMapping(item: RollingNearMissMapping): String {
    return classifyManualViolationWindow(item.adjustedLoad())
}
