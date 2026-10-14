package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.network.cutoff.baseline.CutoffBaseline
import com.acme.transport.network.cutoff.baseline.classifyCutoffBaseline

fun summarizeCutoffBaselinesForEntry(items: MutableList<CutoffBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun derivePrimaryServiceAreaEntriesFromBaseline(items: MutableList<CutoffBaseline>): MutableList<PrimaryServiceAreaEntry> {
    val out = mutableListOf<PrimaryServiceAreaEntry>()
    for (item in items) {
        val derived = newPrimaryServiceAreaEntry(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun derivePrimaryServiceAreaEntry(item: PrimaryServiceAreaEntry): String {
    return classifyCutoffBaseline(item.projectedVolume())
}
