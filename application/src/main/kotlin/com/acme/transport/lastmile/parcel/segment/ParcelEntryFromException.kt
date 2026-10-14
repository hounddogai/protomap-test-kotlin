package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.lastmile.drop.exception.DropException
import com.acme.transport.lastmile.drop.exception.seasonalDropBudgetWeight

fun summarizeDropExceptionsForEntry(items: MutableList<DropException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveParcelEntriesFromException(items: MutableList<DropException>): MutableList<ParcelEntry> {
    val out = mutableListOf<ParcelEntry>()
    for (item in items) {
        val derived = newParcelEntry(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveParcelEntry(item: ParcelEntry): Double = seasonalDropBudgetWeight(2, true)
