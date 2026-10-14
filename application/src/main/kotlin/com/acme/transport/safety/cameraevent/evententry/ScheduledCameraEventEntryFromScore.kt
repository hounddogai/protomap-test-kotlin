package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.safety.nearmiss.schedule.TieredNearMissScore
import com.acme.transport.safety.nearmiss.schedule.tieredNearMissScoreWeight

fun summarizeTieredNearMissScoresForEntry(items: MutableList<TieredNearMissScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveScheduledCameraEventEntriesFromScore(items: MutableList<TieredNearMissScore>): MutableList<ScheduledCameraEventEntry> {
    val out = mutableListOf<ScheduledCameraEventEntry>()
    for (item in items) {
        val derived = newScheduledCameraEventEntry(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleScheduledCameraEventEntry(item: ScheduledCameraEventEntry): Double = tieredNearMissScoreWeight(9, true)
