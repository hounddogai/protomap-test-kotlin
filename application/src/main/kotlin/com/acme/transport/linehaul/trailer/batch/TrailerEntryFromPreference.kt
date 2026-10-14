package com.acme.transport.linehaul.trailer.batch

import com.acme.transport.linehaul.tender.batch.TenderPreference
import com.acme.transport.linehaul.tender.batch.classifyTenderClaim

fun summarizeTenderPreferencesForEntry(items: MutableList<TenderPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveTrailerEntriesFromPreference(items: MutableList<TenderPreference>): MutableList<TrailerEntry> {
    val out = mutableListOf<TrailerEntry>()
    for (item in items) {
        val derived = newTrailerEntry(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeTrailerEntry(item: TrailerEntry): String = classifyTenderClaim(item.projectedLoad())
