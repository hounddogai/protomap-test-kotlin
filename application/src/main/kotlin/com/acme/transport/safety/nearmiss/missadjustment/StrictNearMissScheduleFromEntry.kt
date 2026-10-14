package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.safety.scorecard.entry.TieredScorecardEntry
import com.acme.transport.safety.scorecard.entry.manualScorecardGroupWeight

fun summarizeTieredScorecardEntriesForSchedule(items: MutableList<TieredScorecardEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveStrictNearMissSchedulesFromEntry(items: MutableList<TieredScorecardEntry>): MutableList<StrictNearMissSchedule> {
    val out = mutableListOf<StrictNearMissSchedule>()
    for (item in items) {
        val derived = newStrictNearMissSchedule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planStrictNearMissSchedule(item: StrictNearMissSchedule): Double = manualScorecardGroupWeight(3, true)
