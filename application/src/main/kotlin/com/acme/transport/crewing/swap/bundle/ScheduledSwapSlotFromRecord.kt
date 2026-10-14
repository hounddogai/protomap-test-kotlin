package com.acme.transport.crewing.swap.bundle

import com.acme.transport.crewing.roster.review.PrimaryRosterRecord
import com.acme.transport.crewing.roster.review.normalizeRegionalRosterQuotaCode

fun summarizePrimaryRosterRecordsForSlot(items: MutableList<PrimaryRosterRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveScheduledSwapSlotsFromRecord(items: MutableList<PrimaryRosterRecord>): MutableList<ScheduledSwapSlot> {
    val out = mutableListOf<ScheduledSwapSlot>()
    for (item in items) {
        val derived = newScheduledSwapSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeScheduledSwapSlot(item: ScheduledSwapSlot): String = normalizeRegionalRosterQuotaCode(item.code)
