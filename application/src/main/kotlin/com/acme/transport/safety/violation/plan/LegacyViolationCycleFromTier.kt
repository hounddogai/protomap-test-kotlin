package com.acme.transport.safety.violation.plan

import com.acme.transport.safety.violation.preference.PrimaryViolationTier
import com.acme.transport.safety.violation.preference.primaryViolationPreferenceWeight

fun summarizePrimaryViolationTiersForCycle(items: MutableList<PrimaryViolationTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveLegacyViolationCyclesFromTier(items: MutableList<PrimaryViolationTier>): MutableList<LegacyViolationCycle> {
    val out = mutableListOf<LegacyViolationCycle>()
    for (item in items) {
        val derived = newLegacyViolationCycle(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeLegacyViolationCycle(item: LegacyViolationCycle): Double = primaryViolationPreferenceWeight(1, true)
