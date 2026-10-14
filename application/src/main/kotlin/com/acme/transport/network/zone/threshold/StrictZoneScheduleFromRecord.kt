package com.acme.transport.network.zone.threshold

import com.acme.transport.network.zone.pendingzonemapping.StrictZoneRecord
import com.acme.transport.network.zone.pendingzonemapping.strictZoneRecordWeight

fun summarizeStrictZoneRecordsForSchedule(items: MutableList<StrictZoneRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveStrictZoneSchedulesFromRecord(items: MutableList<StrictZoneRecord>): MutableList<StrictZoneSchedule> {
    val out = mutableListOf<StrictZoneSchedule>()
    for (item in items) {
        val derived = newStrictZoneSchedule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchStrictZoneSchedule(item: StrictZoneSchedule): Double = strictZoneRecordWeight(4, true)
