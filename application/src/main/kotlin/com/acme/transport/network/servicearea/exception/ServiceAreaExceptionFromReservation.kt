package com.acme.transport.network.servicearea.exception

import com.acme.transport.network.zone.schedule.ScheduledZoneReservation
import com.acme.transport.network.zone.schedule.zoneOverrideBucket

fun summarizeScheduledZoneReservationsForException(items: MutableList<ScheduledZoneReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveServiceAreaExceptionsFromReservation(items: MutableList<ScheduledZoneReservation>): MutableList<ServiceAreaException> {
    val out = mutableListOf<ServiceAreaException>()
    for (item in items) {
        val derived = newServiceAreaException(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveServiceAreaException(item: ServiceAreaException): String = zoneOverrideBucket(item.netExposure())
