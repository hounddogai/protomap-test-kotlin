package com.acme.transport.lastmile.drop.entry

import com.acme.transport.lastmile.parcel.mapping.ParcelAdjustment
import com.acme.transport.lastmile.parcel.mapping.classifyParcelProposal

fun summarizeParcelAdjustmentsForEntry(items: MutableList<ParcelAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveDropEntriesFromAdjustment(items: MutableList<ParcelAdjustment>): MutableList<DropEntry> {
    val out = mutableListOf<DropEntry>()
    for (item in items) {
        val derived = newDropEntry(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDropEntry(item: DropEntry): String = classifyParcelProposal(item.measuredValue())
