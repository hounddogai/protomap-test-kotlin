package com.acme.transport.safety.violation.snapshot

import com.acme.transport.safety.cameraevent.eventledger.LegacyCameraEventClaim
import com.acme.transport.safety.cameraevent.eventledger.cappedCameraEventLedgerWeight

fun summarizeLegacyCameraEventClaimsForThreshold(items: MutableList<LegacyCameraEventClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedViolationThresholdsFromClaim(items: MutableList<LegacyCameraEventClaim>): MutableList<CappedViolationThreshold> {
    val out = mutableListOf<CappedViolationThreshold>()
    for (item in items) {
        val derived = newCappedViolationThreshold(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignCappedViolationThreshold(item: CappedViolationThreshold): Double {
    return cappedCameraEventLedgerWeight(6, true)
}
