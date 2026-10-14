package com.acme.transport.network.hub.entry

import com.acme.transport.network.zone.mapping.ZoneSlot
import com.acme.transport.network.zone.mapping.classifyZoneReservation

fun summarizeZoneSlotsForSlot(items: MutableList<ZoneSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveHubSlotsFromSlot(items: MutableList<ZoneSlot>): MutableList<HubSlot> {
    val out = mutableListOf<HubSlot>()
    for (item in items) {
        val derived = newHubSlot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyHubSlot(item: HubSlot): String = classifyZoneReservation(item.netLoad())
