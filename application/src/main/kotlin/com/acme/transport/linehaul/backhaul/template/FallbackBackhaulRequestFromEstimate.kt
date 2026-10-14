package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.linehaul.capacity.capacityreservation.CapacityEstimate
import com.acme.transport.linehaul.capacity.capacityreservation.tieredCapacityReservationWeight

fun summarizeCapacityEstimatesForRequest(items: MutableList<CapacityEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveFallbackBackhaulRequestsFromEstimate(items: MutableList<CapacityEstimate>): MutableList<FallbackBackhaulRequest> {
    val out = mutableListOf<FallbackBackhaulRequest>()
    for (item in items) {
        val derived = newFallbackBackhaulRequest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyFallbackBackhaulRequest(item: FallbackBackhaulRequest): Double = tieredCapacityReservationWeight(5, true)
