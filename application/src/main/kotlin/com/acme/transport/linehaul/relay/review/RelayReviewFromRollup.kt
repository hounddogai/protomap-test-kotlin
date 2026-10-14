package com.acme.transport.linehaul.relay.review

import com.acme.transport.linehaul.capacity.rollup.CapacityRollup
import com.acme.transport.linehaul.capacity.rollup.classifyCapacityCheckpoint

fun summarizeCapacityRollupsForReview(items: MutableList<CapacityRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveRelayReviewsFromRollup(items: MutableList<CapacityRollup>): MutableList<RelayReview> {
    val out = mutableListOf<RelayReview>()
    for (item in items) {
        val derived = newRelayReview(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileRelayReview(item: RelayReview): String = classifyCapacityCheckpoint(item.effectiveVolume())
