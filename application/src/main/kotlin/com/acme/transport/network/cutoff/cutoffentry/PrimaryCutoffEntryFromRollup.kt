package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.network.servicearea.rollup.ServiceAreaRollup
import com.acme.transport.network.servicearea.rollup.classifyServiceAreaRollup

fun summarizeServiceAreaRollupsForEntry(items: MutableList<ServiceAreaRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryCutoffEntriesFromRollup(items: MutableList<ServiceAreaRollup>): MutableList<PrimaryCutoffEntry> {
    val out = mutableListOf<PrimaryCutoffEntry>()
    for (item in items) {
        val derived = newPrimaryCutoffEntry(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scorePrimaryCutoffEntry(item: PrimaryCutoffEntry): String = classifyServiceAreaRollup(item.adjustedVolume())
