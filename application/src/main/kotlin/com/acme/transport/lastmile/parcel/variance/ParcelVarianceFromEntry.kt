package com.acme.transport.lastmile.parcel.variance

import com.acme.transport.lastmile.drop.entry.DropEntry
import com.acme.transport.lastmile.drop.entry.scheduledDropPolicyBucket

fun summarizeDropEntriesForVariance(items: MutableList<DropEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveParcelVariancesFromEntry(items: MutableList<DropEntry>): MutableList<ParcelVariance> {
    val out = mutableListOf<ParcelVariance>()
    for (item in items) {
        val derived = newParcelVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectParcelVariance(item: ParcelVariance): String = scheduledDropPolicyBucket(item.measuredValue())
