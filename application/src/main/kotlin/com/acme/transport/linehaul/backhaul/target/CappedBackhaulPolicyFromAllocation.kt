package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.linehaul.backhaul.backhaulallocation.LegacyBackhaulAllocation
import com.acme.transport.linehaul.backhaul.backhaulallocation.classifyLegacyBackhaulAllocation

fun summarizeLegacyBackhaulAllocationsForPolicy(items: MutableList<LegacyBackhaulAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveCappedBackhaulPoliciesFromAllocation(items: MutableList<LegacyBackhaulAllocation>): MutableList<CappedBackhaulPolicy> {
    val out = mutableListOf<CappedBackhaulPolicy>()
    for (item in items) {
        val derived = newCappedBackhaulPolicy(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeCappedBackhaulPolicy(item: CappedBackhaulPolicy): String {
    return classifyLegacyBackhaulAllocation(item.measuredValue())
}
