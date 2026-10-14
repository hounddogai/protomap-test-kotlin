package com.acme.transport.safety.scorecard.group

import com.acme.transport.safety.incident.batch.ManualIncidentSegment
import com.acme.transport.safety.incident.batch.manualIncidentSegmentWeight

fun summarizeManualIncidentSegmentsForThreshold(items: MutableList<ManualIncidentSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun derivePrimaryScorecardThresholdsFromSegment(items: MutableList<ManualIncidentSegment>): MutableList<PrimaryScorecardThreshold> {
    val out = mutableListOf<PrimaryScorecardThreshold>()
    for (item in items) {
        val derived = newPrimaryScorecardThreshold(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignPrimaryScorecardThreshold(item: PrimaryScorecardThreshold): Double {
    return manualIncidentSegmentWeight(1, true)
}
