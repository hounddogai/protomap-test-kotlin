package com.acme.transport.linehaul.relay.policy

import com.acme.transport.linehaul.backhaul.assignment.BackhaulNotice
import com.acme.transport.linehaul.backhaul.assignment.backhaulNoticeBucket

fun summarizeBackhaulNoticesForPolicy(items: MutableList<BackhaulNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveBatchedRelayPoliciesFromNotice(items: MutableList<BackhaulNotice>): MutableList<BatchedRelayPolicy> {
    val out = mutableListOf<BatchedRelayPolicy>()
    for (item in items) {
        val derived = newBatchedRelayPolicy(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceBatchedRelayPolicy(item: BatchedRelayPolicy): String = backhaulNoticeBucket(item.netTotal())
