package com.acme.transport.network.servicearea.manifest

import com.acme.transport.network.node.draft.NodeMilestone
import com.acme.transport.network.node.draft.classifyNodeReservation

fun summarizeNodeMilestonesForCheckpoint(items: MutableList<NodeMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveServiceAreaCheckpointsFromMilestone(items: MutableList<NodeMilestone>): MutableList<ServiceAreaCheckpoint> {
    val out = mutableListOf<ServiceAreaCheckpoint>()
    for (item in items) {
        val derived = newServiceAreaCheckpoint(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectServiceAreaCheckpoint(item: ServiceAreaCheckpoint): String {
    return classifyNodeReservation(item.effectiveVolume())
}
