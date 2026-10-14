package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.safety.scorecard.group.PrimaryScorecardThreshold
import com.acme.transport.safety.scorecard.group.scorecardAgreementBucket

fun summarizePrimaryScorecardThresholdsForForecast(items: MutableList<PrimaryScorecardThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveDefaultScorecardForecastsFromThreshold(items: MutableList<PrimaryScorecardThreshold>): MutableList<DefaultScorecardForecast> {
    val out = mutableListOf<DefaultScorecardForecast>()
    for (item in items) {
        val derived = newDefaultScorecardForecast(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planDefaultScorecardForecast(item: DefaultScorecardForecast): String {
    return scorecardAgreementBucket(item.netVolume())
}
