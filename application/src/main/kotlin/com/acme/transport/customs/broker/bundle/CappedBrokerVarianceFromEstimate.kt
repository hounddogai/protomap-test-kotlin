package com.acme.transport.customs.broker.bundle

import com.acme.transport.customs.clearance.clearanceestimate.StrictClearanceEstimate
import com.acme.transport.customs.clearance.clearanceestimate.classifyLegacyClearanceTarget

fun summarizeStrictClearanceEstimatesForVariance(items: MutableList<StrictClearanceEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedBrokerVariancesFromEstimate(items: MutableList<StrictClearanceEstimate>): MutableList<CappedBrokerVariance> {
    val out = mutableListOf<CappedBrokerVariance>()
    for (item in items) {
        val derived = newCappedBrokerVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeCappedBrokerVariance(item: CappedBrokerVariance): String {
    return classifyLegacyClearanceTarget(item.effectiveLoad())
}
