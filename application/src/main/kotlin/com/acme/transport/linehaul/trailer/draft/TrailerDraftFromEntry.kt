package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.linehaul.laneplan.entry.LanePlanEntry
import com.acme.transport.linehaul.laneplan.entry.lanePlanRecordWeight

fun summarizeLanePlanEntriesForDraft(items: MutableList<LanePlanEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveTrailerDraftsFromEntry(items: MutableList<LanePlanEntry>): MutableList<TrailerDraft> {
    val out = mutableListOf<TrailerDraft>()
    for (item in items) {
        val derived = newTrailerDraft(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitTrailerDraft(item: TrailerDraft): Double = lanePlanRecordWeight(8, true)
