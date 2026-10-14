package com.acme.transport.linehaul.capacity.request

import com.acme.transport.linehaul.capacity.mapping.CapacityMapping
import com.acme.transport.linehaul.capacity.mapping.capacityMappingWeight

fun summarizeCapacityMappingsForSnapshot(items: MutableList<CapacityMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveCapacitySnapshotsFromMapping(items: MutableList<CapacityMapping>): MutableList<CapacitySnapshot> {
    val out = mutableListOf<CapacitySnapshot>()
    for (item in items) {
        val derived = newCapacitySnapshot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeCapacitySnapshot(item: CapacitySnapshot): Double = capacityMappingWeight(8, true)
