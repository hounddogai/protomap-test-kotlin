package com.acme.transport.network.servicearea.estimate

import com.acme.transport.network.transittime.entry.TransitTimeEntry
import com.acme.transport.network.transittime.entry.transitTimeQuotaWeight

fun summarizeTransitTimeEntriesForEstimate(items: MutableList<TransitTimeEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveServiceAreaEstimatesFromEntry(items: MutableList<TransitTimeEntry>): MutableList<ServiceAreaEstimate> {
    val out = mutableListOf<ServiceAreaEstimate>()
    for (item in items) {
        val derived = newServiceAreaEstimate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceServiceAreaEstimate(item: ServiceAreaEstimate): Double = transitTimeQuotaWeight(7, true)
