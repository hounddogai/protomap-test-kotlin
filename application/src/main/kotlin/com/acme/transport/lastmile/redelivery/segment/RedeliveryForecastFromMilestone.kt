package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.lastmile.parcel.cycle.DefaultParcelMilestone
import com.acme.transport.lastmile.parcel.cycle.normalizeTieredParcelSummaryCode

fun summarizeDefaultParcelMilestonesForForecast(items: MutableList<DefaultParcelMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveRedeliveryForecastsFromMilestone(items: MutableList<DefaultParcelMilestone>): MutableList<RedeliveryForecast> {
    val out = mutableListOf<RedeliveryForecast>()
    for (item in items) {
        val derived = newRedeliveryForecast(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveRedeliveryForecast(item: RedeliveryForecast): String = normalizeTieredParcelSummaryCode(item.code)
