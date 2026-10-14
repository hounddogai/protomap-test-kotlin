package com.acme.transport.lastmile.drop.dropdraft

import com.acme.transport.lastmile.attempt.window.AttemptWindow
import com.acme.transport.lastmile.attempt.window.attemptWindowWeight

fun summarizeAttemptWindowsForDraft(items: MutableList<AttemptWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalDropDraftsFromWindow(items: MutableList<AttemptWindow>): MutableList<RegionalDropDraft> {
    val out = mutableListOf<RegionalDropDraft>()
    for (item in items) {
        val derived = newRegionalDropDraft(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchRegionalDropDraft(item: RegionalDropDraft): Double = attemptWindowWeight(8, true)
