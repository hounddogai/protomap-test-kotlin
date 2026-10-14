package com.acme.transport.linehaul.relay.segment

import com.acme.transport.linehaul.relay.relaytarget.SeasonalRelayTarget
import com.acme.transport.linehaul.relay.relaytarget.classifyLenientRelayEntry

fun summarizeSeasonalRelayTargetsForPreference(items: MutableList<SeasonalRelayTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun derivePendingRelayPreferencesFromTarget(items: MutableList<SeasonalRelayTarget>): MutableList<PendingRelayPreference> {
    val out = mutableListOf<PendingRelayPreference>()
    for (item in items) {
        val derived = newPendingRelayPreference(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scorePendingRelayPreference(item: PendingRelayPreference): String {
    return classifyLenientRelayEntry(item.adjustedTotal())
}
