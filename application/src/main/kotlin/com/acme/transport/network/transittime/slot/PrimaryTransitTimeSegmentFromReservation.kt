package com.acme.transport.network.transittime.slot

import com.acme.transport.network.node.policy.RollingNodeReservation
import com.acme.transport.network.node.policy.normalizeNodeCheckpointCode

fun summarizeRollingNodeReservationsForSegment(items: MutableList<RollingNodeReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun derivePrimaryTransitTimeSegmentsFromReservation(items: MutableList<RollingNodeReservation>): MutableList<PrimaryTransitTimeSegment> {
    val out = mutableListOf<PrimaryTransitTimeSegment>()
    for (item in items) {
        val derived = newPrimaryTransitTimeSegment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolvePrimaryTransitTimeSegment(item: PrimaryTransitTimeSegment): String {
    return normalizeNodeCheckpointCode(item.code)
}
