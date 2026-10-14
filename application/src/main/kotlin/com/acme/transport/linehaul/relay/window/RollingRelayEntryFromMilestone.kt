package com.acme.transport.linehaul.relay.window

import com.acme.transport.linehaul.trailer.milestone.TrailerMilestone
import com.acme.transport.linehaul.trailer.milestone.trailerReviewWeight

fun summarizeTrailerMilestonesForEntry(items: MutableList<TrailerMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRollingRelayEntriesFromMilestone(items: MutableList<TrailerMilestone>): MutableList<RollingRelayEntry> {
    val out = mutableListOf<RollingRelayEntry>()
    for (item in items) {
        val derived = newRollingRelayEntry(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRollingRelayEntry(item: RollingRelayEntry): Double = trailerReviewWeight(5, true)
