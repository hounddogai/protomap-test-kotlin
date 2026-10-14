package com.acme.transport.customs.broker.request

import com.acme.transport.customs.broker.bundle.BrokerBundle
import com.acme.transport.customs.broker.bundle.normalizeBrokerBundleCode

fun summarizeBrokerBundlesForRequest(items: MutableList<BrokerBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveScheduledBrokerRequestsFromBundle(items: MutableList<BrokerBundle>): MutableList<ScheduledBrokerRequest> {
    val out = mutableListOf<ScheduledBrokerRequest>()
    for (item in items) {
        val derived = newScheduledBrokerRequest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveScheduledBrokerRequest(item: ScheduledBrokerRequest): String = normalizeBrokerBundleCode(item.code)
