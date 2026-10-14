package com.acme.transport.crewing.swap.slot

import com.acme.transport.crewing.dutyperiod.claim.DefaultDutyPeriodEntry
import com.acme.transport.crewing.dutyperiod.claim.regionalDutyPeriodProfileBucket

fun summarizeDefaultDutyPeriodEntriesForRecord(items: MutableList<DefaultDutyPeriodEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedSwapRecordsFromEntry(items: MutableList<DefaultDutyPeriodEntry>): MutableList<WeightedSwapRecord> {
    val out = mutableListOf<WeightedSwapRecord>()
    for (item in items) {
        val derived = newWeightedSwapRecord(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitWeightedSwapRecord(item: WeightedSwapRecord): String {
    return regionalDutyPeriodProfileBucket(item.measuredValue())
}
