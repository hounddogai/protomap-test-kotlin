package com.acme.transport.crewing.swap.policy

import com.acme.transport.crewing.availability.availabilitytier.AvailabilityDraft
import com.acme.transport.crewing.availability.availabilitytier.classifyPendingAvailabilityTier

fun summarizeAvailabilityDraftsForEntry(items: MutableList<AvailabilityDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveSwapEntriesFromDraft(items: MutableList<AvailabilityDraft>): MutableList<SwapEntry> {
    val out = mutableListOf<SwapEntry>()
    for (item in items) {
        val derived = newSwapEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleSwapEntry(item: SwapEntry): String = classifyPendingAvailabilityTier(item.effectiveValue())
