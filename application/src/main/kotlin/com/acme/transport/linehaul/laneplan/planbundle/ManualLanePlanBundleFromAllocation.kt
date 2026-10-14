package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.linehaul.backhaul.backhaulallocation.LegacyBackhaulAllocation
import com.acme.transport.linehaul.backhaul.backhaulallocation.classifyAdaptiveBackhaulMilestone

fun summarizeLegacyBackhaulAllocationsForBundle(items: MutableList<LegacyBackhaulAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveManualLanePlanBundlesFromAllocation(items: MutableList<LegacyBackhaulAllocation>): MutableList<ManualLanePlanBundle> {
    val out = mutableListOf<ManualLanePlanBundle>()
    for (item in items) {
        val derived = newManualLanePlanBundle(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchManualLanePlanBundle(item: ManualLanePlanBundle): String {
    return classifyAdaptiveBackhaulMilestone(item.measuredValue())
}
