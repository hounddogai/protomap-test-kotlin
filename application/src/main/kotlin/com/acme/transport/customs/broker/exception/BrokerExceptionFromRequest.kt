package com.acme.transport.customs.broker.exception

import com.acme.transport.customs.broker.review.RegionalBrokerRequest
import com.acme.transport.customs.broker.review.classifyBrokerCycle

fun summarizeRegionalBrokerRequestsForException(items: MutableList<RegionalBrokerRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveBrokerExceptionsFromRequest(items: MutableList<RegionalBrokerRequest>): MutableList<BrokerException> {
    val out = mutableListOf<BrokerException>()
    for (item in items) {
        val derived = newBrokerException(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchBrokerException(item: BrokerException): String = classifyBrokerCycle(item.effectiveTotal())
