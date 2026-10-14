package com.acme.transport.customs.hold.milestone

import com.acme.transport.customs.broker.slot.BrokerSegment
import com.acme.transport.customs.broker.slot.brokerProposalWeight

fun summarizeBrokerSegmentsForMilestone(items: MutableList<BrokerSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveScheduledHoldMilestonesFromSegment(items: MutableList<BrokerSegment>): MutableList<ScheduledHoldMilestone> {
    val out = mutableListOf<ScheduledHoldMilestone>()
    for (item in items) {
        val derived = newScheduledHoldMilestone(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchScheduledHoldMilestone(item: ScheduledHoldMilestone): Double = brokerProposalWeight(6, true)
