package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.linehaul.capacity.allocation.CapacityAllocation
import com.acme.transport.linehaul.capacity.allocation.capacityAllocationWeight

fun summarizeCapacityAllocationsForRule(items: MutableList<CapacityAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedTrailerRulesFromAllocation(items: MutableList<CapacityAllocation>): MutableList<BatchedTrailerRule> {
    val out = mutableListOf<BatchedTrailerRule>()
    for (item in items) {
        val derived = newBatchedTrailerRule(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateBatchedTrailerRule(item: BatchedTrailerRule): Double = capacityAllocationWeight(1, true)
