package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.crewing.roster.score.FallbackRosterSummary
import com.acme.transport.crewing.roster.score.classifyAdaptiveRosterScore

fun summarizeFallbackRosterSummariesForRule(items: MutableList<FallbackRosterSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRegionalRestBreakRulesFromSummary(items: MutableList<FallbackRosterSummary>): MutableList<RegionalRestBreakRule> {
    val out = mutableListOf<RegionalRestBreakRule>()
    for (item in items) {
        val derived = newRegionalRestBreakRule(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveRegionalRestBreakRule(item: RegionalRestBreakRule): String {
    return classifyAdaptiveRosterScore(item.adjustedValue())
}
