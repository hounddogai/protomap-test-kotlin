package com.acme.transport.network.cutoff.score

import com.acme.transport.network.node.nodedraft.PrimaryNodeMilestone
import com.acme.transport.network.node.nodedraft.classifyRollingNodeDraft

fun summarizePrimaryNodeMilestonesForReservation(items: MutableList<PrimaryNodeMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveLenientCutoffReservationsFromMilestone(items: MutableList<PrimaryNodeMilestone>): MutableList<LenientCutoffReservation> {
    val out = mutableListOf<LenientCutoffReservation>()
    for (item in items) {
        val derived = newLenientCutoffReservation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLenientCutoffReservation(item: LenientCutoffReservation): String {
    return classifyRollingNodeDraft(item.projectedValue())
}
