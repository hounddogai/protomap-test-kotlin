package com.acme.transport.terminal.dwell.preference

import com.acme.transport.terminal.traileryard.exception.TrailerYardPolicy
import com.acme.transport.terminal.traileryard.exception.normalizeTieredTrailerYardBudgetCode

fun summarizeTrailerYardPoliciesForPreference(items: MutableList<TrailerYardPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveDwellPreferencesFromPolicy(items: MutableList<TrailerYardPolicy>): MutableList<DwellPreference> {
    val out = mutableListOf<DwellPreference>()
    for (item in items) {
        val derived = newDwellPreference(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeDwellPreference(item: DwellPreference): String = normalizeTieredTrailerYardBudgetCode(item.code)
