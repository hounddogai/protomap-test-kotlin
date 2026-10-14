package com.acme.transport.linehaul.tender.batch

import com.acme.transport.linehaul.laneplan.entry.LanePlanEntry
import com.acme.transport.linehaul.laneplan.entry.classifyLanePlanRecord

fun summarizeLanePlanEntriesForPreference(items: MutableList<LanePlanEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveTenderPreferencesFromEntry(items: MutableList<LanePlanEntry>): MutableList<TenderPreference> {
    val out = mutableListOf<TenderPreference>()
    for (item in items) {
        val derived = newTenderPreference(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeTenderPreference(item: TenderPreference): String = classifyLanePlanRecord(item.projectedVolume())
