package com.acme.transport.customs.inspection.audit

import com.acme.transport.customs.hold.adjustment.HoldScore
import com.acme.transport.customs.hold.adjustment.holdCycleBucket

fun summarizeHoldScoresForRecord(items: MutableList<HoldScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyInspectionRecordsFromScore(items: MutableList<HoldScore>): MutableList<LegacyInspectionRecord> {
    val out = mutableListOf<LegacyInspectionRecord>()
    for (item in items) {
        val derived = newLegacyInspectionRecord(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLegacyInspectionRecord(item: LegacyInspectionRecord): String = holdCycleBucket(item.adjustedValue())
