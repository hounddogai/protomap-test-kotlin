package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.linehaul.backhaul.adjustment.ManualBackhaulAllocation
import com.acme.transport.linehaul.backhaul.adjustment.classifyGlobalBackhaulMetric

fun summarizeManualBackhaulAllocationsForOverride(items: MutableList<ManualBackhaulAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveStrictBackhaulOverridesFromAllocation(items: MutableList<ManualBackhaulAllocation>): MutableList<StrictBackhaulOverride> {
    val out = mutableListOf<StrictBackhaulOverride>()
    for (item in items) {
        val derived = newStrictBackhaulOverride(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateStrictBackhaulOverride(item: StrictBackhaulOverride): String {
    return classifyGlobalBackhaulMetric(item.adjustedLoad())
}
