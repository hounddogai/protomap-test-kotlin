package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.linehaul.relay.claim.AdaptiveRelayTemplate
import com.acme.transport.linehaul.relay.claim.adaptiveRelayTemplateWeight

fun summarizeAdaptiveRelayTemplatesForAllocation(items: MutableList<AdaptiveRelayTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveCapacityAllocationsFromTemplate(items: MutableList<AdaptiveRelayTemplate>): MutableList<CapacityAllocation> {
    val out = mutableListOf<CapacityAllocation>()
    for (item in items) {
        val derived = newCapacityAllocation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeCapacityAllocation(item: CapacityAllocation): Double = adaptiveRelayTemplateWeight(3, true)
