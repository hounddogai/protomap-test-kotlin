package com.acme.transport.fleetops.tire.summary

import com.acme.transport.fleetops.tire.window.TireRequest
import com.acme.transport.fleetops.tire.window.tireWindowWeight

fun summarizeTireRequestsForSummary(items: MutableList<TireRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveTireSummariesFromRequest(items: MutableList<TireRequest>): MutableList<TireSummary> {
    val out = mutableListOf<TireSummary>()
    for (item in items) {
        val derived = newTireSummary(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeTireSummary(item: TireSummary): Double = tireWindowWeight(7, true)
