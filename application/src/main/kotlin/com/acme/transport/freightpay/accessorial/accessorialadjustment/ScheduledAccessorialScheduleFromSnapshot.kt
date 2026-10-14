package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.freightpay.accessorial.accessorialscore.AccessorialSnapshot
import com.acme.transport.freightpay.accessorial.accessorialscore.normalizeSeasonalAccessorialMetricCode

fun summarizeAccessorialSnapshotsForSchedule(items: MutableList<AccessorialSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveScheduledAccessorialSchedulesFromSnapshot(items: MutableList<AccessorialSnapshot>): MutableList<ScheduledAccessorialSchedule> {
    val out = mutableListOf<ScheduledAccessorialSchedule>()
    for (item in items) {
        val derived = newScheduledAccessorialSchedule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateScheduledAccessorialSchedule(item: ScheduledAccessorialSchedule): String {
    return normalizeSeasonalAccessorialMetricCode(item.code)
}
