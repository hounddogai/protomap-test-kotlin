package com.acme.transport.customs.clearance.clearancerequest

import com.acme.transport.customs.broker.checkpoint.CappedBrokerMapping
import com.acme.transport.customs.broker.checkpoint.cappedBrokerMappingWeight

fun summarizeCappedBrokerMappingsForRequest(items: MutableList<CappedBrokerMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun derivePrimaryClearanceRequestsFromMapping(items: MutableList<CappedBrokerMapping>): MutableList<PrimaryClearanceRequest> {
    val out = mutableListOf<PrimaryClearanceRequest>()
    for (item in items) {
        val derived = newPrimaryClearanceRequest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planPrimaryClearanceRequest(item: PrimaryClearanceRequest): Double = cappedBrokerMappingWeight(3, true)
