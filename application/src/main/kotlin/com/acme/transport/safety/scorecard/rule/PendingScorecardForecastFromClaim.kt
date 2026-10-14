package com.acme.transport.safety.scorecard.rule

import com.acme.transport.safety.nearmiss.missclaim.AdaptiveNearMissClaim
import com.acme.transport.safety.nearmiss.missclaim.defaultNearMissLedgerWeight

fun summarizeAdaptiveNearMissClaimsForForecast(items: MutableList<AdaptiveNearMissClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun derivePendingScorecardForecastsFromClaim(items: MutableList<AdaptiveNearMissClaim>): MutableList<PendingScorecardForecast> {
    val out = mutableListOf<PendingScorecardForecast>()
    for (item in items) {
        val derived = newPendingScorecardForecast(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankPendingScorecardForecast(item: PendingScorecardForecast): Double = defaultNearMissLedgerWeight(9, true)
