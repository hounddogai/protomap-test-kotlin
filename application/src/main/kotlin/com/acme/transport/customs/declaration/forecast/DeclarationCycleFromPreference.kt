package com.acme.transport.customs.declaration.forecast

import com.acme.transport.customs.clearance.preference.ClearancePreference
import com.acme.transport.customs.clearance.preference.clearancePreferenceBucket

fun summarizeClearancePreferencesForCycle(items: MutableList<ClearancePreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveDeclarationCyclesFromPreference(items: MutableList<ClearancePreference>): MutableList<DeclarationCycle> {
    val out = mutableListOf<DeclarationCycle>()
    for (item in items) {
        val derived = newDeclarationCycle(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyDeclarationCycle(item: DeclarationCycle): String = clearancePreferenceBucket(item.effectiveVolume())
