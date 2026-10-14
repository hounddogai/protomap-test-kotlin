package com.acme.transport.network.transittime.timerequest

import com.acme.transport.network.transittime.scheduledtimebatch.PrimaryTransitTimeWindow
import com.acme.transport.network.transittime.scheduledtimebatch.primaryTransitTimeWindowWeight

fun summarizePrimaryTransitTimeWindowsForRequest(items: MutableList<PrimaryTransitTimeWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveLenientTransitTimeRequestsFromWindow(items: MutableList<PrimaryTransitTimeWindow>): MutableList<LenientTransitTimeRequest> {
    val out = mutableListOf<LenientTransitTimeRequest>()
    for (item in items) {
        val derived = newLenientTransitTimeRequest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyLenientTransitTimeRequestExtra(item: LenientTransitTimeRequest): Double {
    return primaryTransitTimeWindowWeight(1, true)
}
