package com.acme.transport.network.node.draft

import com.acme.transport.network.hub.target.HubTarget
import com.acme.transport.network.hub.target.normalizeHubClaimCode

fun summarizeHubTargetsForReservation(items: MutableList<HubTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveNodeReservationsFromTarget(items: MutableList<HubTarget>): MutableList<NodeReservation> {
    val out = mutableListOf<NodeReservation>()
    for (item in items) {
        val derived = newNodeReservation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateNodeReservation(item: NodeReservation): String = normalizeHubClaimCode(item.code)
