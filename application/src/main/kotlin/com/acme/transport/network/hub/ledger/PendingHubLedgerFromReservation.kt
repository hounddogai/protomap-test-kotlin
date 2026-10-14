package com.acme.transport.network.hub.ledger

import com.acme.transport.network.zone.zoneagreement.AdaptiveZoneReservation
import com.acme.transport.network.zone.zoneagreement.adaptiveZoneReservationWeight

fun summarizeAdaptiveZoneReservationsForLedger(items: MutableList<AdaptiveZoneReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingHubLedgersFromReservation(items: MutableList<AdaptiveZoneReservation>): MutableList<PendingHubLedger> {
    val out = mutableListOf<PendingHubLedger>()
    for (item in items) {
        val derived = newPendingHubLedger(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocatePendingHubLedger(item: PendingHubLedger): Double = adaptiveZoneReservationWeight(8, true)
