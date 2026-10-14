package com.acme.transport.linehaul.laneplan.checkpoint

import com.acme.transport.linehaul.capacity.mapping.CapacityVariance
import com.acme.transport.linehaul.capacity.mapping.normalizeCapacityVarianceCode

fun summarizeCapacityVariancesForCheckpoint(items: MutableList<CapacityVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveLanePlanCheckpointsFromVariance(items: MutableList<CapacityVariance>): MutableList<LanePlanCheckpoint> {
    val out = mutableListOf<LanePlanCheckpoint>()
    for (item in items) {
        val derived = newLanePlanCheckpoint(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveLanePlanCheckpoint(item: LanePlanCheckpoint): String = normalizeCapacityVarianceCode(item.code)
