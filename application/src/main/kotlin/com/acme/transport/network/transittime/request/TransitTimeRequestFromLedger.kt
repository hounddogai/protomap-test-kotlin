package com.acme.transport.network.transittime.request

import com.acme.transport.network.transittime.ledger.TransitTimeLedger
import com.acme.transport.network.transittime.ledger.defaultTransitTimePolicyWeight

fun summarizeTransitTimeLedgersForRequest(items: MutableList<TransitTimeLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveTransitTimeRequestsFromLedger(items: MutableList<TransitTimeLedger>): MutableList<TransitTimeRequest> {
    val out = mutableListOf<TransitTimeRequest>()
    for (item in items) {
        val derived = newTransitTimeRequest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveTransitTimeRequest(item: TransitTimeRequest): Double = defaultTransitTimePolicyWeight(6, true)
