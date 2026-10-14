package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.safety.coaching.baseline.RegionalCoachingCheckpoint
import com.acme.transport.safety.coaching.baseline.normalizeManualCoachingBaselineCode

fun summarizeRegionalCoachingCheckpointsForSchedule(items: MutableList<RegionalCoachingCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveLenientNearMissSchedulesFromCheckpoint(items: MutableList<RegionalCoachingCheckpoint>): MutableList<LenientNearMissSchedule> {
    val out = mutableListOf<LenientNearMissSchedule>()
    for (item in items) {
        val derived = newLenientNearMissSchedule(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeLenientNearMissSchedule(item: LenientNearMissSchedule): String {
    return normalizeManualCoachingBaselineCode(item.code)
}
