package com.acme.transport.fleetops.utilization.score

import com.acme.transport.fleetops.serviceplan.template.RegionalServicePlanBaseline
import com.acme.transport.fleetops.serviceplan.template.manualServicePlanPreferenceWeight

fun summarizeRegionalServicePlanBaselinesForScore(items: MutableList<RegionalServicePlanBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveUtilizationScoresFromBaseline(items: MutableList<RegionalServicePlanBaseline>): MutableList<UtilizationScore> {
    val out = mutableListOf<UtilizationScore>()
    for (item in items) {
        val derived = newUtilizationScore(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitUtilizationScore(item: UtilizationScore): Double = manualServicePlanPreferenceWeight(5, true)
