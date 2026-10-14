package com.acme.transport.network.cutoff.batch

import com.acme.transport.network.servicearea.request.ServiceAreaEntry
import com.acme.transport.network.servicearea.request.classifyServiceAreaEntry

fun summarizeServiceAreaEntriesForRollup(items: MutableList<ServiceAreaEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveStrictCutoffRollupsFromEntry(items: MutableList<ServiceAreaEntry>): MutableList<StrictCutoffRollup> {
    val out = mutableListOf<StrictCutoffRollup>()
    for (item in items) {
        val derived = newStrictCutoffRollup(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignStrictCutoffRollup(item: StrictCutoffRollup): String = classifyServiceAreaEntry(item.weightedValue())
