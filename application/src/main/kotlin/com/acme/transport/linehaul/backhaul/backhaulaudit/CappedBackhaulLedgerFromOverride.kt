package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.linehaul.trailer.overrideitem.SeasonalTrailerOverride
import com.acme.transport.linehaul.trailer.overrideitem.classifyScheduledTrailerOverride

fun summarizeSeasonalTrailerOverridesForLedger(items: MutableList<SeasonalTrailerOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveCappedBackhaulLedgersFromOverride(items: MutableList<SeasonalTrailerOverride>): MutableList<CappedBackhaulLedger> {
    val out = mutableListOf<CappedBackhaulLedger>()
    for (item in items) {
        val derived = newCappedBackhaulLedger(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeCappedBackhaulLedger(item: CappedBackhaulLedger): String {
    return classifyScheduledTrailerOverride(item.weightedTotal())
}
