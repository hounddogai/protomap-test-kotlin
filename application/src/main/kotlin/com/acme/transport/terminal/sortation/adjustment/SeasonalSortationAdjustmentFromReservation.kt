package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.terminal.traileryard.reservation.LegacyTrailerYardReservation
import com.acme.transport.terminal.traileryard.reservation.legacyTrailerYardReservationWeight

fun summarizeLegacyTrailerYardReservationsForAdjustment(items: MutableList<LegacyTrailerYardReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveSeasonalSortationAdjustmentsFromReservation(items: MutableList<LegacyTrailerYardReservation>): MutableList<SeasonalSortationAdjustment> {
    val out = mutableListOf<SeasonalSortationAdjustment>()
    for (item in items) {
        val derived = newSeasonalSortationAdjustment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeSeasonalSortationAdjustment(item: SeasonalSortationAdjustment): Double {
    return legacyTrailerYardReservationWeight(6, true)
}
