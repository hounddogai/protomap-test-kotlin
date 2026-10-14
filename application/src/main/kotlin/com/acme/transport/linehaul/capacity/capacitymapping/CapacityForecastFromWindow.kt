package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.linehaul.backhaul.backhaulreservation.LegacyBackhaulWindow
import com.acme.transport.linehaul.backhaul.backhaulreservation.legacyBackhaulReservationBucket

fun summarizeLegacyBackhaulWindowsForForecast(items: MutableList<LegacyBackhaulWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveCapacityForecastsFromWindow(items: MutableList<LegacyBackhaulWindow>): MutableList<CapacityForecast> {
    val out = mutableListOf<CapacityForecast>()
    for (item in items) {
        val derived = newCapacityForecast(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyCapacityForecastExtra(item: CapacityForecast): String {
    return legacyBackhaulReservationBucket(item.measuredValue())
}
