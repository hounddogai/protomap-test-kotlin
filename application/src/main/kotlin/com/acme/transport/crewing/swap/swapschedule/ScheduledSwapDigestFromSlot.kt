package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.crewing.roster.preference.RosterSlot
import com.acme.transport.crewing.roster.preference.normalizeRosterSlotCode

fun summarizeRosterSlotsForDigest(items: MutableList<RosterSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveScheduledSwapDigestsFromSlot(items: MutableList<RosterSlot>): MutableList<ScheduledSwapDigest> {
    val out = mutableListOf<ScheduledSwapDigest>()
    for (item in items) {
        val derived = newScheduledSwapDigest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateScheduledSwapDigest(item: ScheduledSwapDigest): String = normalizeRosterSlotCode(item.code)
