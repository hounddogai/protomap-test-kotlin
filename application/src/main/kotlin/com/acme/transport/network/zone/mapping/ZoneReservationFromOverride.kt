package com.acme.transport.network.zone.mapping

import com.acme.transport.network.hub.overrideitem.HubOverride
import com.acme.transport.network.hub.overrideitem.hubGroupWeight

fun summarizeHubOverridesForReservation(items: MutableList<HubOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveZoneReservationsFromOverride(items: MutableList<HubOverride>): MutableList<ZoneReservation> {
    val out = mutableListOf<ZoneReservation>()
    for (item in items) {
        val derived = newZoneReservation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectZoneReservation(item: ZoneReservation): Double = hubGroupWeight(6, true)
