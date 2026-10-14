package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.lastmile.redelivery.entry.RedeliveryEntry
import com.acme.transport.lastmile.redelivery.entry.normalizeRedeliveryBundleCode

fun summarizeRedeliveryEntriesForReservation(items: MutableList<RedeliveryEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveRedeliveryReservationsFromEntry(items: MutableList<RedeliveryEntry>): MutableList<RedeliveryReservation> {
    val out = mutableListOf<RedeliveryReservation>()
    for (item in items) {
        val derived = newRedeliveryReservation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchRedeliveryReservation(item: RedeliveryReservation): String = normalizeRedeliveryBundleCode(item.code)
