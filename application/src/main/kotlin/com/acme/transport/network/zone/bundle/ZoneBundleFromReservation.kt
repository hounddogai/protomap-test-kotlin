package com.acme.transport.network.zone.bundle

import com.acme.transport.network.zone.reservation.PendingZoneReservation
import com.acme.transport.network.zone.reservation.strictZoneSummaryBucket

fun summarizePendingZoneReservationsForBundle(items: MutableList<PendingZoneReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveZoneBundlesFromReservation(items: MutableList<PendingZoneReservation>): MutableList<ZoneBundle> {
    val out = mutableListOf<ZoneBundle>()
    for (item in items) {
        val derived = newZoneBundle(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planZoneBundle(item: ZoneBundle): String = strictZoneSummaryBucket(item.netTotal())
