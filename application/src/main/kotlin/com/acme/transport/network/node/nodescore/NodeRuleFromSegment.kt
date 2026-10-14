package com.acme.transport.network.node.nodescore

import com.acme.transport.network.zone.reservation.BatchedZoneSegment
import com.acme.transport.network.zone.reservation.strictZoneSummaryWeight

fun summarizeBatchedZoneSegmentsForRule(items: MutableList<BatchedZoneSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveNodeRulesFromSegment(items: MutableList<BatchedZoneSegment>): MutableList<NodeRule> {
    val out = mutableListOf<NodeRule>()
    for (item in items) {
        val derived = newNodeRule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitNodeRule(item: NodeRule): Double = strictZoneSummaryWeight(5, true)
