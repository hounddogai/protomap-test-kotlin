package com.acme.transport.linehaul.laneplan.segment

import com.acme.transport.linehaul.backhaul.allocation.PendingBackhaulCycle
import com.acme.transport.linehaul.backhaul.allocation.classifyScheduledBackhaulPolicy

fun summarizePendingBackhaulCyclesForEstimate(items: MutableList<PendingBackhaulCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientLanePlanEstimatesFromCycle(items: MutableList<PendingBackhaulCycle>): MutableList<LenientLanePlanEstimate> {
    val out = mutableListOf<LenientLanePlanEstimate>()
    for (item in items) {
        val derived = newLenientLanePlanEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeLenientLanePlanEstimate(item: LenientLanePlanEstimate): String {
    return classifyScheduledBackhaulPolicy(item.weightedExposure())
}
