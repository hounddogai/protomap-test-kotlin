package com.acme.transport.crewing.swap.assignment

import com.acme.transport.crewing.roster.rosterrule.SeasonalRosterTemplate
import com.acme.transport.crewing.roster.rosterrule.normalizeRosterGroupCode

fun summarizeSeasonalRosterTemplatesForAllocation(items: MutableList<SeasonalRosterTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveSwapAllocationsFromTemplate(items: MutableList<SeasonalRosterTemplate>): MutableList<AdaptiveSwapAllocation> {
    val out = mutableListOf<AdaptiveSwapAllocation>()
    for (item in items) {
        val derived = newAdaptiveSwapAllocation(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceAdaptiveSwapAllocation(item: AdaptiveSwapAllocation): String = normalizeRosterGroupCode(item.code)
