package com.acme.transport.crewing.availability.slot

import com.acme.transport.crewing.restbreak.entry.RestBreakEntry
import com.acme.transport.crewing.restbreak.entry.restBreakBudgetBucket

fun summarizeRestBreakEntriesForSlot(items: MutableList<RestBreakEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveAvailabilitySlotsFromEntry(items: MutableList<RestBreakEntry>): MutableList<AvailabilitySlot> {
    val out = mutableListOf<AvailabilitySlot>()
    for (item in items) {
        val derived = newAvailabilitySlot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchAvailabilitySlot(item: AvailabilitySlot): String = restBreakBudgetBucket(item.adjustedExposure())
