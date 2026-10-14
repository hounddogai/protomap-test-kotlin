package com.acme.transport.linehaul.relay.claim

import com.acme.transport.linehaul.capacity.capacitymilestone.BatchedCapacityMilestone
import com.acme.transport.linehaul.capacity.capacitymilestone.normalizeBatchedCapacityMilestoneCode

fun summarizeBatchedCapacityMilestonesForAssignment(items: MutableList<BatchedCapacityMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveTieredRelayAssignmentsFromMilestone(items: MutableList<BatchedCapacityMilestone>): MutableList<TieredRelayAssignment> {
    val out = mutableListOf<TieredRelayAssignment>()
    for (item in items) {
        val derived = newTieredRelayAssignment(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveTieredRelayAssignment(item: TieredRelayAssignment): String {
    return normalizeBatchedCapacityMilestoneCode(item.code)
}
