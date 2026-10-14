package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.lastmile.parcel.request.ParcelRequest
import com.acme.transport.lastmile.parcel.request.classifyParcelTarget

fun summarizeParcelRequestsForEntry(items: MutableList<ParcelRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun derivePrimaryDropEntriesFromRequest(items: MutableList<ParcelRequest>): MutableList<PrimaryDropEntry> {
    val out = mutableListOf<PrimaryDropEntry>()
    for (item in items) {
        val derived = newPrimaryDropEntry(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolvePrimaryDropEntry(item: PrimaryDropEntry): String = classifyParcelTarget(item.measuredValue())
