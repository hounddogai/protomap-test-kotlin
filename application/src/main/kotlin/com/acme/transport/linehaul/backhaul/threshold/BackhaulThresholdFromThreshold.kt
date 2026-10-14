package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.linehaul.laneplan.cycle.CappedLanePlanThreshold
import com.acme.transport.linehaul.laneplan.cycle.classifyLanePlanWindow

fun summarizeCappedLanePlanThresholdsForThreshold(items: MutableList<CappedLanePlanThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBackhaulThresholdsFromThreshold(items: MutableList<CappedLanePlanThreshold>): MutableList<BackhaulThreshold> {
    val out = mutableListOf<BackhaulThreshold>()
    for (item in items) {
        val derived = newBackhaulThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeBackhaulThreshold(item: BackhaulThreshold): String = classifyLanePlanWindow(item.measuredValue())
