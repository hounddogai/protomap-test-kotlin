package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.lastmile.attempt.metric.GlobalAttemptCheckpoint
import com.acme.transport.lastmile.attempt.metric.legacyAttemptAuditWeight

fun summarizeGlobalAttemptCheckpointsForSchedule(items: MutableList<GlobalAttemptCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRegionalParcelSchedulesFromCheckpoint(items: MutableList<GlobalAttemptCheckpoint>): MutableList<RegionalParcelSchedule> {
    val out = mutableListOf<RegionalParcelSchedule>()
    for (item in items) {
        val derived = newRegionalParcelSchedule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectRegionalParcelSchedule(item: RegionalParcelSchedule): Double = legacyAttemptAuditWeight(5, true)
