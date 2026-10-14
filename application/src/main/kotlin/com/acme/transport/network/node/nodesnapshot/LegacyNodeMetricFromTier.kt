package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.network.zone.zonetier.PrimaryZoneTier
import com.acme.transport.network.zone.zonetier.zoneLedgerBucket

fun summarizePrimaryZoneTiersForMetric(items: MutableList<PrimaryZoneTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveLegacyNodeMetricsFromTier(items: MutableList<PrimaryZoneTier>): MutableList<LegacyNodeMetric> {
    val out = mutableListOf<LegacyNodeMetric>()
    for (item in items) {
        val derived = newLegacyNodeMetric(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchLegacyNodeMetric(item: LegacyNodeMetric): String = zoneLedgerBucket(item.weightedTotal())
