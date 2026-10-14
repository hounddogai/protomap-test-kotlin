package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.safety.cameraevent.eventledger.CappedCameraEventLedger
import com.acme.transport.safety.cameraevent.eventledger.cappedCameraEventLedgerBucket

fun summarizeCappedCameraEventLedgersForClaim(items: MutableList<CappedCameraEventLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveCappedIncidentClaimsFromLedger(items: MutableList<CappedCameraEventLedger>): MutableList<CappedIncidentClaim> {
    val out = mutableListOf<CappedIncidentClaim>()
    for (item in items) {
        val derived = newCappedIncidentClaim(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeCappedIncidentClaim(item: CappedIncidentClaim): String {
    return cappedCameraEventLedgerBucket(item.weightedTotal())
}
