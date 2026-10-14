package com.acme.transport.network.node.nodegroup

import com.acme.transport.network.zone.threshold.ZoneThreshold
import com.acme.transport.network.zone.threshold.zoneThresholdWeight

fun summarizeZoneThresholdsForGroup(items: MutableList<ZoneThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveCappedNodeGroupsFromThreshold(items: MutableList<ZoneThreshold>): MutableList<CappedNodeGroup> {
    val out = mutableListOf<CappedNodeGroup>()
    for (item in items) {
        val derived = newCappedNodeGroup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateCappedNodeGroup(item: CappedNodeGroup): Double = zoneThresholdWeight(5, true)
