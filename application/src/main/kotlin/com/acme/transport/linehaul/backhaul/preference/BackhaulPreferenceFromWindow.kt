package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.linehaul.relay.assignment.RelayWindow
import com.acme.transport.linehaul.relay.assignment.adaptiveRelayAssignmentBucket

fun summarizeRelayWindowsForPreference(items: MutableList<RelayWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveBackhaulPreferencesFromWindow(items: MutableList<RelayWindow>): MutableList<BackhaulPreference> {
    val out = mutableListOf<BackhaulPreference>()
    for (item in items) {
        val derived = newBackhaulPreference(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileBackhaulPreference(item: BackhaulPreference): String {
    return adaptiveRelayAssignmentBucket(item.measuredValue())
}
