package com.acme.transport.linehaul.tender.preference

import com.acme.transport.linehaul.relay.summary.RelaySummary
import com.acme.transport.linehaul.relay.summary.pendingRelayReservationWeight

fun summarizeRelaySummariesForMilestone(items: MutableList<RelaySummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTieredTenderMilestonesFromSummary(items: MutableList<RelaySummary>): MutableList<TieredTenderMilestone> {
    val out = mutableListOf<TieredTenderMilestone>()
    for (item in items) {
        val derived = newTieredTenderMilestone(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planTieredTenderMilestone(item: TieredTenderMilestone): Double = pendingRelayReservationWeight(2, true)
