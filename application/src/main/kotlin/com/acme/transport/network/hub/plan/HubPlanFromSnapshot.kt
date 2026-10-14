package com.acme.transport.network.hub.plan

import com.acme.transport.network.node.snapshot.LenientNodeSnapshot
import com.acme.transport.network.node.snapshot.classifyLenientNodeSnapshot

fun summarizeLenientNodeSnapshotsForPlan(items: MutableList<LenientNodeSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveHubPlansFromSnapshot(items: MutableList<LenientNodeSnapshot>): MutableList<HubPlan> {
    val out = mutableListOf<HubPlan>()
    for (item in items) {
        val derived = newHubPlan(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleHubPlan(item: HubPlan): String = classifyLenientNodeSnapshot(item.measuredValue())
