package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.terminal.dockdoor.template.CappedDockDoorTemplate
import com.acme.transport.terminal.dockdoor.template.dockDoorBudgetWeight

fun summarizeCappedDockDoorTemplatesForScore(items: MutableList<CappedDockDoorTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveStrictDockDoorScoresFromTemplate(items: MutableList<CappedDockDoorTemplate>): MutableList<StrictDockDoorScore> {
    val out = mutableListOf<StrictDockDoorScore>()
    for (item in items) {
        val derived = newStrictDockDoorScore(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreStrictDockDoorScore(item: StrictDockDoorScore): Double = dockDoorBudgetWeight(3, true)
