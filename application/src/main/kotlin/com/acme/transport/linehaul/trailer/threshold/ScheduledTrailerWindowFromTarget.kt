package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.linehaul.laneplan.bundle.WeightedLanePlanTarget
import com.acme.transport.linehaul.laneplan.bundle.classifyWeightedLanePlanBundle

fun summarizeWeightedLanePlanTargetsForWindow(items: MutableList<WeightedLanePlanTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledTrailerWindowsFromTarget(items: MutableList<WeightedLanePlanTarget>): MutableList<ScheduledTrailerWindow> {
    val out = mutableListOf<ScheduledTrailerWindow>()
    for (item in items) {
        val derived = newScheduledTrailerWindow(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignScheduledTrailerWindowExtra(item: ScheduledTrailerWindow): String {
    return classifyWeightedLanePlanBundle(item.adjustedValue())
}
