package com.acme.transport.terminal.dwell.summary

import com.acme.transport.terminal.traileryard.yardreservation.BatchedTrailerYardReservation
import com.acme.transport.terminal.traileryard.yardreservation.normalizeScheduledTrailerYardScoreCode

fun summarizeBatchedTrailerYardReservationsForForecast(items: MutableList<BatchedTrailerYardReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveManualDwellForecastsFromReservation(items: MutableList<BatchedTrailerYardReservation>): MutableList<ManualDwellForecast> {
    val out = mutableListOf<ManualDwellForecast>()
    for (item in items) {
        val derived = newManualDwellForecast(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectManualDwellForecast(item: ManualDwellForecast): String {
    return normalizeScheduledTrailerYardScoreCode(item.code)
}
