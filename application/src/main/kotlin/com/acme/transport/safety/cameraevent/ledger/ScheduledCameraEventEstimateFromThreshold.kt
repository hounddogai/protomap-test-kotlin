package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.safety.scorecard.group.PrimaryScorecardThreshold
import com.acme.transport.safety.scorecard.group.normalizeRollingScorecardGroupCode

fun summarizePrimaryScorecardThresholdsForEstimate(items: MutableList<PrimaryScorecardThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveScheduledCameraEventEstimatesFromThreshold(items: MutableList<PrimaryScorecardThreshold>): MutableList<ScheduledCameraEventEstimate> {
    val out = mutableListOf<ScheduledCameraEventEstimate>()
    for (item in items) {
        val derived = newScheduledCameraEventEstimate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignScheduledCameraEventEstimate(item: ScheduledCameraEventEstimate): String {
    return normalizeRollingScorecardGroupCode(item.code)
}
