package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.network.node.bundle.NodeSegment
import com.acme.transport.network.node.bundle.nodeProposalBucket

fun summarizeNodeSegmentsForThreshold(items: MutableList<NodeSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveSeasonalServiceAreaThresholdsFromSegment(items: MutableList<NodeSegment>): MutableList<SeasonalServiceAreaThreshold> {
    val out = mutableListOf<SeasonalServiceAreaThreshold>()
    for (item in items) {
        val derived = newSeasonalServiceAreaThreshold(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateSeasonalServiceAreaThreshold(item: SeasonalServiceAreaThreshold): String {
    return nodeProposalBucket(item.weightedExposure())
}
