package com.acme.transport.network.cutoff.entry

import com.acme.transport.network.zone.mapping.ZoneSlot
import com.acme.transport.network.zone.mapping.classifyZoneTemplate

fun summarizeZoneSlotsForEntry(items: MutableList<ZoneSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveCutoffEntriesFromSlot(items: MutableList<ZoneSlot>): MutableList<CutoffEntry> {
    val out = mutableListOf<CutoffEntry>()
    for (item in items) {
        val derived = newCutoffEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeCutoffEntry(item: CutoffEntry): String = classifyZoneTemplate(item.measuredValue())
