package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.linehaul.capacity.mapping.CapacityVariance
import com.acme.transport.linehaul.capacity.mapping.capacityMappingWeight

fun summarizeCapacityVariancesForMapping(items: MutableList<CapacityVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveTenderMappingsFromVariance(items: MutableList<CapacityVariance>): MutableList<TenderMapping> {
    val out = mutableListOf<TenderMapping>()
    for (item in items) {
        val derived = newTenderMapping(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveTenderMapping(item: TenderMapping): Double = capacityMappingWeight(8, true)
