package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.terminal.crossdock.reservation.RegionalCrossDockReservation
import com.acme.transport.terminal.crossdock.reservation.normalizeAdaptiveCrossDockThresholdCode

fun summarizeRegionalCrossDockReservationsForOverride(items: MutableList<RegionalCrossDockReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveSeasonalTrailerYardOverridesFromReservation(items: MutableList<RegionalCrossDockReservation>): MutableList<SeasonalTrailerYardOverride> {
    val out = mutableListOf<SeasonalTrailerYardOverride>()
    for (item in items) {
        val derived = newSeasonalTrailerYardOverride(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleSeasonalTrailerYardOverride(item: SeasonalTrailerYardOverride): String {
    return normalizeAdaptiveCrossDockThresholdCode(item.code)
}
