package com.acme.transport.terminal.traileryard.template

import com.acme.transport.terminal.sortation.preference.SortationPreference
import com.acme.transport.terminal.sortation.preference.classifySortationPreference

fun summarizeSortationPreferencesForBudget(items: MutableList<SortationPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveFallbackTrailerYardBudgetsFromPreference(items: MutableList<SortationPreference>): MutableList<FallbackTrailerYardBudget> {
    val out = mutableListOf<FallbackTrailerYardBudget>()
    for (item in items) {
        val derived = newFallbackTrailerYardBudget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateFallbackTrailerYardBudget(item: FallbackTrailerYardBudget): String {
    return classifySortationPreference(item.measuredValue())
}
