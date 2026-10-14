package com.acme.transport.network.node.overrideitem

import com.acme.transport.network.hub.policy.HubThreshold
import com.acme.transport.network.hub.policy.globalHubPolicyBucket

fun summarizeHubThresholdsForPolicy(items: MutableList<HubThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveBatchedNodePoliciesFromThreshold(items: MutableList<HubThreshold>): MutableList<BatchedNodePolicy> {
    val out = mutableListOf<BatchedNodePolicy>()
    for (item in items) {
        val derived = newBatchedNodePolicy(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignBatchedNodePolicy(item: BatchedNodePolicy): String = globalHubPolicyBucket(item.netVolume())
