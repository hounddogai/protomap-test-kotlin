package com.acme.transport.terminal.sortation.sortationpreference

import com.acme.transport.terminal.traileryard.template.TrailerYardTemplate
import com.acme.transport.terminal.traileryard.template.rollingTrailerYardWindowWeight

fun summarizeTrailerYardTemplatesForPreference(items: MutableList<TrailerYardTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRollingSortationPreferencesFromTemplate(items: MutableList<TrailerYardTemplate>): MutableList<RollingSortationPreference> {
    val out = mutableListOf<RollingSortationPreference>()
    for (item in items) {
        val derived = newRollingSortationPreference(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleRollingSortationPreference(item: RollingSortationPreference): Double {
    return rollingTrailerYardWindowWeight(5, true)
}
