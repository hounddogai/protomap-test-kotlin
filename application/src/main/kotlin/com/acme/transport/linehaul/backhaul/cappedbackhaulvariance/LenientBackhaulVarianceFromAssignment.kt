package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import com.acme.transport.linehaul.capacity.capacityassignment.CappedCapacityAssignment
import com.acme.transport.linehaul.capacity.capacityassignment.normalizeCapacityAdjustmentCode

fun summarizeCappedCapacityAssignmentsForVariance(items: MutableList<CappedCapacityAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveLenientBackhaulVariancesFromAssignment(items: MutableList<CappedCapacityAssignment>): MutableList<LenientBackhaulVariance> {
    val out = mutableListOf<LenientBackhaulVariance>()
    for (item in items) {
        val derived = newLenientBackhaulVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planLenientBackhaulVariance(item: LenientBackhaulVariance): String {
    return normalizeCapacityAdjustmentCode(item.code)
}
