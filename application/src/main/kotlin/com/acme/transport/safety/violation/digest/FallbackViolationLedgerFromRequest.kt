package com.acme.transport.safety.violation.digest

import com.acme.transport.safety.incident.request.FallbackIncidentRequest
import com.acme.transport.safety.incident.request.fallbackIncidentRequestBucket

fun summarizeFallbackIncidentRequestsForLedger(items: MutableList<FallbackIncidentRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveFallbackViolationLedgersFromRequest(items: MutableList<FallbackIncidentRequest>): MutableList<FallbackViolationLedger> {
    val out = mutableListOf<FallbackViolationLedger>()
    for (item in items) {
        val derived = newFallbackViolationLedger(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeFallbackViolationLedger(item: FallbackViolationLedger): String {
    return fallbackIncidentRequestBucket(item.effectiveVolume())
}
