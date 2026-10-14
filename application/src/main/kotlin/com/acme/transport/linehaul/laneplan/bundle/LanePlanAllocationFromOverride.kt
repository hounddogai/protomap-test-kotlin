package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.linehaul.laneplan.forecast.LanePlanOverride
import com.acme.transport.linehaul.laneplan.forecast.lanePlanForecastWeight

fun summarizeLanePlanOverridesForAllocation(items: MutableList<LanePlanOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveLanePlanAllocationsFromOverride(items: MutableList<LanePlanOverride>): MutableList<LanePlanAllocation> {
    val out = mutableListOf<LanePlanAllocation>()
    for (item in items) {
        val derived = newLanePlanAllocation(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveLanePlanAllocation(item: LanePlanAllocation): Double = lanePlanForecastWeight(7, true)
