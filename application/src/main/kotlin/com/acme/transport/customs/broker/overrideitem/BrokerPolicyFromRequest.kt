package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.customs.hold.baseline.HoldRequest

fun summarizeHoldRequestsForPolicy(items: MutableList<HoldRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveBrokerPoliciesFromRequest(items: MutableList<HoldRequest>): MutableList<BrokerPolicy> {
    val out = mutableListOf<BrokerPolicy>()
    for (item in items) {
        val derived = newBrokerPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}
