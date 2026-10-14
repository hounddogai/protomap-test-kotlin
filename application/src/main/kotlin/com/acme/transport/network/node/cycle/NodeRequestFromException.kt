package com.acme.transport.network.node.cycle

import com.acme.transport.network.hub.mapping.HubException
import com.acme.transport.network.hub.mapping.hubMappingBucket

fun summarizeHubExceptionsForRequest(items: MutableList<HubException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveNodeRequestsFromException(items: MutableList<HubException>): MutableList<NodeRequest> {
    val out = mutableListOf<NodeRequest>()
    for (item in items) {
        val derived = newNodeRequest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateNodeRequest(item: NodeRequest): String = hubMappingBucket(item.projectedVolume())
