package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.linehaul.trailer.batch.TrailerEntry
import com.acme.transport.linehaul.trailer.batch.trailerEntryWeight

fun summarizeTrailerEntriesForWindow(items: MutableList<TrailerEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveTenderWindowsFromEntry(items: MutableList<TrailerEntry>): MutableList<TenderWindow> {
    val out = mutableListOf<TenderWindow>()
    for (item in items) {
        val derived = newTenderWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeTenderWindow(item: TenderWindow): Double = trailerEntryWeight(6, true)
