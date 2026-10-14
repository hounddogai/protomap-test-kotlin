package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.safety.cameraevent.adjustment.LegacyCameraEventAdjustment
import com.acme.transport.safety.cameraevent.adjustment.lenientCameraEventLedgerWeight

fun summarizeLegacyCameraEventAdjustmentsForSchedule(items: MutableList<LegacyCameraEventAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveScorecardSchedulesFromAdjustment(items: MutableList<LegacyCameraEventAdjustment>): MutableList<ScorecardSchedule> {
    val out = mutableListOf<ScorecardSchedule>()
    for (item in items) {
        val derived = newScorecardSchedule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveScorecardSchedule(item: ScorecardSchedule): Double = lenientCameraEventLedgerWeight(1, true)
