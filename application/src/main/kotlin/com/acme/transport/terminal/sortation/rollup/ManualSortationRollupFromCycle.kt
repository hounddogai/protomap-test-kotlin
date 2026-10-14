package com.acme.transport.terminal.sortation.rollup

import com.acme.transport.terminal.traileryard.yardcycle.PrimaryTrailerYardCycle
import com.acme.transport.terminal.traileryard.yardcycle.legacyTrailerYardBundleWeight

fun summarizePrimaryTrailerYardCyclesForRollup(items: MutableList<PrimaryTrailerYardCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveManualSortationRollupsFromCycle(items: MutableList<PrimaryTrailerYardCycle>): MutableList<ManualSortationRollup> {
    val out = mutableListOf<ManualSortationRollup>()
    for (item in items) {
        val derived = newManualSortationRollup(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchManualSortationRollup(item: ManualSortationRollup): Double = legacyTrailerYardBundleWeight(3, true)
