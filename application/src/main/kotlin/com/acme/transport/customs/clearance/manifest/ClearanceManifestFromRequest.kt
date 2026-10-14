package com.acme.transport.customs.clearance.manifest

import com.acme.transport.customs.broker.budget.BrokerRequest
import com.acme.transport.customs.broker.budget.brokerAdjustmentWeight

fun summarizeBrokerRequestsForManifest(items: MutableList<BrokerRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveClearanceManifestsFromRequest(items: MutableList<BrokerRequest>): MutableList<ClearanceManifest> {
    val out = mutableListOf<ClearanceManifest>()
    for (item in items) {
        val derived = newClearanceManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileClearanceManifest(item: ClearanceManifest): Double = brokerAdjustmentWeight(1, true)
