package com.acme.transport.crewing.roster.quota

import com.acme.transport.crewing.availability.ledger.PendingAvailabilityForecast
import com.acme.transport.crewing.availability.ledger.normalizeAvailabilityLedgerCode

fun summarizePendingAvailabilityForecastsForRequest(items: MutableList<PendingAvailabilityForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveRosterRequestsFromForecast(items: MutableList<PendingAvailabilityForecast>): MutableList<RosterRequest> {
    val out = mutableListOf<RosterRequest>()
    for (item in items) {
        val derived = newRosterRequest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeRosterRequest(item: RosterRequest): String = normalizeAvailabilityLedgerCode(item.code)
