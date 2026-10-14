package com.acme.transport.crewing.restbreak.threshold

import com.acme.transport.crewing.dutyperiod.periodsnapshot.PendingDutyPeriodSnapshot
import com.acme.transport.crewing.dutyperiod.periodsnapshot.dutyPeriodAuditWeight

fun summarizePendingDutyPeriodSnapshotsForThreshold(items: MutableList<PendingDutyPeriodSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveTieredRestBreakThresholdsFromSnapshot(items: MutableList<PendingDutyPeriodSnapshot>): MutableList<TieredRestBreakThreshold> {
    val out = mutableListOf<TieredRestBreakThreshold>()
    for (item in items) {
        val derived = newTieredRestBreakThreshold(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveTieredRestBreakThreshold(item: TieredRestBreakThreshold): Double = dutyPeriodAuditWeight(1, true)
