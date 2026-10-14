package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.linehaul.laneplan.checkpoint.LanePlanSlot
import com.acme.transport.linehaul.laneplan.checkpoint.lanePlanCheckpointBucket

fun summarizeLanePlanSlotsForAssignment(items: MutableList<LanePlanSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveRelayAssignmentsFromSlot(items: MutableList<LanePlanSlot>): MutableList<AdaptiveRelayAssignment> {
    val out = mutableListOf<AdaptiveRelayAssignment>()
    for (item in items) {
        val derived = newAdaptiveRelayAssignment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveAdaptiveRelayAssignment(item: AdaptiveRelayAssignment): String {
    return lanePlanCheckpointBucket(item.measuredValue())
}
