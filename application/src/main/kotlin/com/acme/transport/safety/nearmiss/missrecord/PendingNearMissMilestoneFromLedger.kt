package com.acme.transport.safety.nearmiss.missrecord

import com.acme.transport.safety.coaching.ledger.PendingCoachingLedger
import com.acme.transport.safety.coaching.ledger.classifyCoachingVariance

fun summarizePendingCoachingLedgersForMilestone(items: MutableList<PendingCoachingLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun derivePendingNearMissMilestonesFromLedger(items: MutableList<PendingCoachingLedger>): MutableList<PendingNearMissMilestone> {
    val out = mutableListOf<PendingNearMissMilestone>()
    for (item in items) {
        val derived = newPendingNearMissMilestone(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolvePendingNearMissMilestone(item: PendingNearMissMilestone): String {
    return classifyCoachingVariance(item.measuredValue())
}
