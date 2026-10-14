package com.acme.transport.terminal.traileryard.forecast

import com.acme.transport.terminal.dockdoor.claim.ScheduledDockDoorClaim
import com.acme.transport.terminal.dockdoor.claim.scheduledDockDoorClaimWeight

fun summarizeScheduledDockDoorClaimsForForecast(items: MutableList<ScheduledDockDoorClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveTrailerYardForecastsFromClaim(items: MutableList<ScheduledDockDoorClaim>): MutableList<TrailerYardForecast> {
    val out = mutableListOf<TrailerYardForecast>()
    for (item in items) {
        val derived = newTrailerYardForecast(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeTrailerYardForecast(item: TrailerYardForecast): Double = scheduledDockDoorClaimWeight(9, true)
