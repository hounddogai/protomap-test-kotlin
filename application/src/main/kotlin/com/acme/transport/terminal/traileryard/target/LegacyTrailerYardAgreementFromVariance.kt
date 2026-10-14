package com.acme.transport.terminal.traileryard.target

import com.acme.transport.terminal.sortation.sortationvariance.SeasonalSortationVariance
import com.acme.transport.terminal.sortation.sortationvariance.batchedSortationOverrideWeight

fun summarizeSeasonalSortationVariancesForAgreement(items: MutableList<SeasonalSortationVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveLegacyTrailerYardAgreementsFromVariance(items: MutableList<SeasonalSortationVariance>): MutableList<LegacyTrailerYardAgreement> {
    val out = mutableListOf<LegacyTrailerYardAgreement>()
    for (item in items) {
        val derived = newLegacyTrailerYardAgreement(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyLegacyTrailerYardAgreement(item: LegacyTrailerYardAgreement): Double {
    return batchedSortationOverrideWeight(7, true)
}
