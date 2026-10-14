package com.acme.transport.emissions.idling.proposal

import com.acme.transport.emissions.carbonreport.rollup.CarbonReportRollup
import com.acme.transport.emissions.carbonreport.rollup.pendingCarbonReportWindowWeight

fun summarizeCarbonReportRollupsForSegment(items: MutableList<CarbonReportRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingIdlingSegmentsFromRollup(items: MutableList<CarbonReportRollup>): MutableList<RollingIdlingSegment> {
    val out = mutableListOf<RollingIdlingSegment>()
    for (item in items) {
        val derived = newRollingIdlingSegment(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeRollingIdlingSegment(item: RollingIdlingSegment): Double = pendingCarbonReportWindowWeight(2, true)
