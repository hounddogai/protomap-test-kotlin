package com.acme.transport.network.zone.reservation

import com.acme.transport.network.hub.hubsummary.ScheduledHubVariance
import com.acme.transport.network.hub.hubsummary.normalizeLenientHubSummaryCode

fun summarizeScheduledHubVariancesForReservation(items: MutableList<ScheduledHubVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun derivePendingZoneReservationsFromVariance(items: MutableList<ScheduledHubVariance>): MutableList<PendingZoneReservation> {
    val out = mutableListOf<PendingZoneReservation>()
    for (item in items) {
        val derived = newPendingZoneReservation(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitPendingZoneReservation(item: PendingZoneReservation): String = normalizeLenientHubSummaryCode(item.code)
