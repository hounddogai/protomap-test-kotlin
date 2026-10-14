package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.network.zone.reservation.PendingZoneReservation
import com.acme.transport.network.zone.reservation.strictZoneSummaryBucket

fun summarizePendingZoneReservationsForTarget(items: MutableList<PendingZoneReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingServiceAreaTargetsFromReservation(items: MutableList<PendingZoneReservation>): MutableList<RollingServiceAreaTarget> {
    val out = mutableListOf<RollingServiceAreaTarget>()
    for (item in items) {
        val derived = newRollingServiceAreaTarget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeRollingServiceAreaTarget(item: RollingServiceAreaTarget): String {
    return strictZoneSummaryBucket(item.weightedLoad())
}
