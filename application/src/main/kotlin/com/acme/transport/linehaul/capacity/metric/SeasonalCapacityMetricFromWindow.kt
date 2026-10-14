package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.linehaul.laneplan.cycle.LanePlanWindow
import com.acme.transport.linehaul.laneplan.cycle.classifyCappedLanePlanCycle

fun summarizeLanePlanWindowsForMetric(items: MutableList<LanePlanWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveSeasonalCapacityMetricsFromWindow(items: MutableList<LanePlanWindow>): MutableList<SeasonalCapacityMetric> {
    val out = mutableListOf<SeasonalCapacityMetric>()
    for (item in items) {
        val derived = newSeasonalCapacityMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceSeasonalCapacityMetric(item: SeasonalCapacityMetric): String {
    return classifyCappedLanePlanCycle(item.netVolume())
}
