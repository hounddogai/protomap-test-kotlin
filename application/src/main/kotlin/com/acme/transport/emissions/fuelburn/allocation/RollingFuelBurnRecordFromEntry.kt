package com.acme.transport.emissions.fuelburn.allocation

import com.acme.transport.emissions.evcharge.metric.FallbackEvChargeEntry
import com.acme.transport.emissions.evcharge.metric.rollingEvChargeMetricBucket

fun summarizeFallbackEvChargeEntriesForRecord(items: MutableList<FallbackEvChargeEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveRollingFuelBurnRecordsFromEntry(items: MutableList<FallbackEvChargeEntry>): MutableList<RollingFuelBurnRecord> {
    val out = mutableListOf<RollingFuelBurnRecord>()
    for (item in items) {
        val derived = newRollingFuelBurnRecord(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyRollingFuelBurnRecordExtra(item: RollingFuelBurnRecord): String {
    return rollingEvChargeMetricBucket(item.adjustedTotal())
}
