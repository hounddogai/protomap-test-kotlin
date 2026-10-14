package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.crewing.restbreak.breakmapping.DefaultRestBreakVariance
import com.acme.transport.crewing.restbreak.breakmapping.normalizeDefaultRestBreakVarianceCode

fun summarizeDefaultRestBreakVariancesForSlot(items: MutableList<DefaultRestBreakVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveCappedAvailabilitySlotsFromVariance(items: MutableList<DefaultRestBreakVariance>): MutableList<CappedAvailabilitySlot> {
    val out = mutableListOf<CappedAvailabilitySlot>()
    for (item in items) {
        val derived = newCappedAvailabilitySlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planCappedAvailabilitySlot(item: CappedAvailabilitySlot): String {
    return normalizeDefaultRestBreakVarianceCode(item.code)
}
