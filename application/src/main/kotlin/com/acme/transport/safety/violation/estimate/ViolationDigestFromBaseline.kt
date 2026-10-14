package com.acme.transport.safety.violation.estimate

import com.acme.transport.safety.incident.slot.TieredIncidentBaseline
import com.acme.transport.safety.incident.slot.normalizeLegacyIncidentEntryCode

fun summarizeTieredIncidentBaselinesForDigest(items: MutableList<TieredIncidentBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveViolationDigestsFromBaseline(items: MutableList<TieredIncidentBaseline>): MutableList<ViolationDigest> {
    val out = mutableListOf<ViolationDigest>()
    for (item in items) {
        val derived = newViolationDigest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignViolationDigest(item: ViolationDigest): String = normalizeLegacyIncidentEntryCode(item.code)
