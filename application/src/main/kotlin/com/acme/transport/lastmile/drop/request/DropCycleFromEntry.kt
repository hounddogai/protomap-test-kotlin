package com.acme.transport.lastmile.drop.request

import com.acme.transport.lastmile.parcel.milestone.PrimaryParcelEntry
import com.acme.transport.lastmile.parcel.milestone.primaryParcelEntryWeight

fun summarizePrimaryParcelEntriesForCycle(items: MutableList<PrimaryParcelEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveDropCyclesFromEntry(items: MutableList<PrimaryParcelEntry>): MutableList<DropCycle> {
    val out = mutableListOf<DropCycle>()
    for (item in items) {
        val derived = newDropCycle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveDropCycle(item: DropCycle): Double = primaryParcelEntryWeight(9, true)
