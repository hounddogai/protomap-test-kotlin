package com.acme.transport.lastmile.locker.tier

import com.acme.transport.lastmile.locker.metric.ManualLockerMetric
import com.acme.transport.lastmile.locker.metric.normalizeGlobalLockerRecordCode

fun summarizeManualLockerMetricsForTier(items: MutableList<ManualLockerMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLockerTiersFromMetric(items: MutableList<ManualLockerMetric>): MutableList<LockerTier> {
    val out = mutableListOf<LockerTier>()
    for (item in items) {
        val derived = newLockerTier(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyLockerTier(item: LockerTier): String = normalizeGlobalLockerRecordCode(item.code)
