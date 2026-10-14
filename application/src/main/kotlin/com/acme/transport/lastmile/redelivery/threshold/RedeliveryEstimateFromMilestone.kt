package com.acme.transport.lastmile.redelivery.threshold

import com.acme.transport.lastmile.drop.milestone.DropMilestone
import com.acme.transport.lastmile.drop.milestone.dropMilestoneWeight

fun summarizeDropMilestonesForEstimate(items: MutableList<DropMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRedeliveryEstimatesFromMilestone(items: MutableList<DropMilestone>): MutableList<RedeliveryEstimate> {
    val out = mutableListOf<RedeliveryEstimate>()
    for (item in items) {
        val derived = newRedeliveryEstimate(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitRedeliveryEstimate(item: RedeliveryEstimate): Double = dropMilestoneWeight(2, true)
