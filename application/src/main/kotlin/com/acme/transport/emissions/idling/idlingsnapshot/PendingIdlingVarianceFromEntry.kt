package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.emissions.idling.idlingvariance.LenientIdlingEntry
import com.acme.transport.emissions.idling.idlingvariance.classifyIdlingDraft

fun summarizeLenientIdlingEntriesForVariance(items: MutableList<LenientIdlingEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun derivePendingIdlingVariancesFromEntry(items: MutableList<LenientIdlingEntry>): MutableList<PendingIdlingVariance> {
    val out = mutableListOf<PendingIdlingVariance>()
    for (item in items) {
        val derived = newPendingIdlingVariance(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settlePendingIdlingVariance(item: PendingIdlingVariance): String {
    return classifyIdlingDraft(item.adjustedExposure())
}
