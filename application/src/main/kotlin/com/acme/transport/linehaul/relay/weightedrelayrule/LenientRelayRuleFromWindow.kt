package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.linehaul.laneplan.planwindow.StrictLanePlanWindow
import com.acme.transport.linehaul.laneplan.planwindow.strictLanePlanWindowWeight

fun summarizeStrictLanePlanWindowsForRule(items: MutableList<StrictLanePlanWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveLenientRelayRulesFromWindow(items: MutableList<StrictLanePlanWindow>): MutableList<LenientRelayRule> {
    val out = mutableListOf<LenientRelayRule>()
    for (item in items) {
        val derived = newLenientRelayRule(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitLenientRelayRule(item: LenientRelayRule): Double = strictLanePlanWindowWeight(3, true)
