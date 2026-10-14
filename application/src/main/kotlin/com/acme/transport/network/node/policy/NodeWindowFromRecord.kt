package com.acme.transport.network.node.policy

import com.acme.transport.network.hub.recorditem.HubRecord
import com.acme.transport.network.hub.recorditem.hubTemplateWeight

fun summarizeHubRecordsForWindow(items: MutableList<HubRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveNodeWindowsFromRecord(items: MutableList<HubRecord>): MutableList<NodeWindow> {
    val out = mutableListOf<NodeWindow>()
    for (item in items) {
        val derived = newNodeWindow(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveNodeWindow(item: NodeWindow): Double = hubTemplateWeight(2, true)
