package com.acme.transport.network.transittime.forecast

import com.acme.transport.network.zone.zoneagreement.ScheduledZoneNotice
import com.acme.transport.network.zone.zoneagreement.normalizeAdaptiveZoneReservationCode

fun summarizeScheduledZoneNoticesForForecast(items: MutableList<ScheduledZoneNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveTransitTimeForecastsFromNotice(items: MutableList<ScheduledZoneNotice>): MutableList<TransitTimeForecast> {
    val out = mutableListOf<TransitTimeForecast>()
    for (item in items) {
        val derived = newTransitTimeForecast(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeTransitTimeForecast(item: TransitTimeForecast): String {
    return normalizeAdaptiveZoneReservationCode(item.code)
}
