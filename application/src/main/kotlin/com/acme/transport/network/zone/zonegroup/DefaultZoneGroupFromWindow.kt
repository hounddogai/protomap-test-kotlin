package com.acme.transport.network.zone.zonegroup

import com.acme.transport.network.hub.forecast.HubWindow
import com.acme.transport.network.hub.forecast.normalizePendingHubEntryCode

fun summarizeHubWindowsForGroup(items: MutableList<HubWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveDefaultZoneGroupsFromWindow(items: MutableList<HubWindow>): MutableList<DefaultZoneGroup> {
    val out = mutableListOf<DefaultZoneGroup>()
    for (item in items) {
        val derived = newDefaultZoneGroup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planDefaultZoneGroup(item: DefaultZoneGroup): String = normalizePendingHubEntryCode(item.code)
